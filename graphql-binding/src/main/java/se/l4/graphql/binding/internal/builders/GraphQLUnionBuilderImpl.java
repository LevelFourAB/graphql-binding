package se.l4.graphql.binding.internal.builders;

import java.util.HashSet;
import java.util.Set;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import se.l4.commons.types.matching.ClassMatchingConcurrentHashMap;
import se.l4.commons.types.matching.ClassMatchingMap;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLUnionBuilder;

public class GraphQLUnionBuilderImpl
	implements GraphQLUnionBuilder
{
	private final GraphQLResolverContext context;

	private final GraphQLUnionType.Builder builder;

	private final Set<String> fields;
	private final ClassMatchingMap<Object, String> implementations;

	private Breadcrumb breadcrumb;

	private String name;

	public GraphQLUnionBuilderImpl(
		GraphQLResolverContext context
	)
	{
		this.context = context;

		breadcrumb = Breadcrumb.empty();
		this.fields = new HashSet<>();
		builder = GraphQLUnionType.newUnionType();

		implementations = new ClassMatchingConcurrentHashMap<>();
	}

	@Override
	public GraphQLUnionBuilder over(TypeRef type)
	{
		this.breadcrumb = Breadcrumb.forType(type);

		this.name = context.requestOutputTypeName(type);
		builder.name(name);
		builder.description(context.getDescription(type));
		return this;
	}

	@Override
	public GraphQLUnionBuilder setName(String name)
	{
		// TODO: Verify name uniqueness
		this.name = name;
		builder.name(name);
		return this;
	}

	@Override
	public GraphQLUnionBuilder setDescription(String description)
	{
		builder.description(description);
		return this;
	}

	@Override
	public GraphQLUnionBuilder addPossibleType(TypeRef type)
	{
		ResolvedGraphQLType<? extends GraphQLOutputType> resolved = context.resolveOutput(type);
		GraphQLOutputType output = resolved.getGraphQLType();

		if(! (output instanceof GraphQLObjectType))
		{
			Breadcrumb typeCrumb = Breadcrumb.forType(type);
			throw context.newError(
				breadcrumb,
				"Possible types in a union must resolve to an object type, but `"
				+ type.toTypeName() + "` resolved to " + output.getClass().getSimpleName()
			);
		}

		builder.possibleType((GraphQLObjectType) output);
		implementations.put(type.getErasedType(),  resolved.getGraphQLType().getName());
		return this;
	}

	@Override
	public GraphQLUnionType build()
	{
		builder.typeResolver(new TypeResolverImpl(implementations));
		return builder.build();
	}

	private static class TypeResolverImpl
		implements TypeResolver
	{
		private final ClassMatchingMap<Object, String> types;

		public TypeResolverImpl(ClassMatchingMap<Object, String> types)
		{
			this.types = types;
		}

		@Override
		public GraphQLObjectType getType(TypeResolutionEnvironment env)
		{
			return types.getBest(env.getObject().getClass())
				.map(name -> env.getSchema().getObjectType(name))
				.orElseThrow(() -> new GraphQLMappingException(
					"The type `" + env.getObject().getClass()
					+ "` does not have a GraphQL type"
				));
		}
	}
}
