package se.l4.graphql.binding.internal.builders;

import java.util.HashSet;
import java.util.Set;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import se.l4.commons.types.matching.ClassMatchingConcurrentHashMap;
import se.l4.commons.types.matching.ClassMatchingMap;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLInterfaceBuilder;

public class GraphQLInterfaceBuilderImpl
	implements GraphQLInterfaceBuilder
{
	private final GraphQLResolverContext context;

	private final GraphQLInterfaceType.Builder builder;

	private final Set<String> fields;
	private final ClassMatchingMap<Object, String> implementations;

	private Breadcrumb breadcrumb;

	private String name;

	public GraphQLInterfaceBuilderImpl(
		GraphQLResolverContext context
	)
	{
		this.context = context;

		breadcrumb = Breadcrumb.empty();
		this.fields = new HashSet<>();
		builder = GraphQLInterfaceType.newInterface();

		implementations = new ClassMatchingConcurrentHashMap<>();
	}

	@Override
	public GraphQLInterfaceBuilder over(TypeRef type)
	{
		this.breadcrumb = Breadcrumb.forType(type);

		this.name = context.requestOutputTypeName(type);
		builder.name(name);
		builder.description(context.getDescription(type));
		return this;
	}

	@Override
	public GraphQLInterfaceBuilder setName(String name)
	{
		// TODO: Verify name uniqueness
		this.name = name;
		builder.name(name);
		return this;
	}

	@Override
	public GraphQLInterfaceBuilder setDescription(String description)
	{
		builder.description(description);
		return this;
	}

	@Override
	public GraphQLFieldBuilder<GraphQLInterfaceBuilder> newField()
	{
		return new GraphQLFieldBuilderImpl<GraphQLInterfaceBuilder>(
			context,
			breadcrumb,
			this,
			(field, fetcher) -> {
				if(! fields.add(field.getName()))
				{
					throw context.newError("Field name `" + field.getName() + "` is not unique");
				}

				builder.field(field);
			}
		);
	}

	@Override
	public GraphQLInterfaceBuilder addImplementation(TypeRef type)
	{
		implementations.put(type.getErasedType(), context.requestOutputTypeName(type));
		return this;
	}

	@Override
	public GraphQLInterfaceType build()
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
