package se.l4.graphql.binding.internal.builders;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLInterfaceBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixin;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixinEncounter;
import se.l4.ylem.types.matching.ClassMatchingConcurrentHashMap;
import se.l4.ylem.types.matching.ClassMatchingMap;
import se.l4.ylem.types.matching.MutableClassMatchingMap;
import se.l4.ylem.types.reflect.TypeRef;

public class GraphQLInterfaceBuilderImpl
	implements GraphQLInterfaceBuilder
{
	private final GraphQLResolverContext context;
	private final List<GraphQLObjectMixin> mixins;

	private final GraphQLInterfaceType.Builder builder;

	private final Set<String> fields;
	private final MutableClassMatchingMap<Object, String> implementations;

	private TypeRef type;
	private Breadcrumb breadcrumb;

	private String name;

	public GraphQLInterfaceBuilderImpl(
		List<GraphQLObjectMixin> mixins,
		GraphQLResolverContext context
	)
	{
		this.mixins = mixins;
		this.context = context;

		breadcrumb = Breadcrumb.empty();
		this.fields = new HashSet<>();
		builder = GraphQLInterfaceType.newInterface();

		implementations = new ClassMatchingConcurrentHashMap<>();
	}

	@Override
	public GraphQLInterfaceBuilder over(TypeRef type)
	{
		this.type = type;
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
		if(type != null)
		{
			GraphQLObjectMixinEncounter encounter = new GraphQLObjectMixinEncounter()
			{

				public GraphQLFieldBuilder<?> newField()
				{
					return GraphQLInterfaceBuilderImpl.this.newField();
				}

				@Override
				public TypeRef getType()
				{
					return type;
				}

				@Override
				public GraphQLResolverContext getContext()
				{
					return context;
				}
			};

			for(GraphQLObjectMixin mixin : mixins)
			{
				if(mixin.supportsOutputMixin(type))
				{
					mixin.mixin(encounter);
				}
			}
		}

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
