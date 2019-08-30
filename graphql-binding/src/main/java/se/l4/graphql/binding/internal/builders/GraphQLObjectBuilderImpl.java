package se.l4.graphql.binding.internal.builders;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.query.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLObjectMixin;
import se.l4.graphql.binding.resolver.query.GraphQLObjectMixinEncounter;

public class GraphQLObjectBuilderImpl
	implements GraphQLObjectBuilder
{
	private final GraphQLResolverContext context;
	private final List<GraphQLObjectMixin> mixins;

	private final GraphQLCodeRegistry.Builder code;

	private final GraphQLObjectType.Builder builder;

	private final Set<String> fields;

	private TypeRef type;
	private Breadcrumb breadcrumb;

	private String name;

	public GraphQLObjectBuilderImpl(
		List<GraphQLObjectMixin> mixins,
		GraphQLResolverContext context,
		GraphQLCodeRegistry.Builder code
	)
	{
		this.mixins = mixins;
		this.context = context;
		this.code = code;

		breadcrumb = Breadcrumb.empty();
		this.fields = new HashSet<>();
		builder = GraphQLObjectType.newObject();
	}

	@Override
	public GraphQLObjectBuilder over(TypeRef type)
	{
		this.type = type;
		this.breadcrumb = Breadcrumb.forType(type);

		this.name = context.getTypeName(type);
		builder.name(name);
		builder.description(context.getDescription(type));
		return this;
	}

	@Override
	public GraphQLObjectBuilder setName(String name)
	{
		// TODO: Verify name uniqueness
		this.name = name;
		builder.name(name);
		return this;
	}

	@Override
	public GraphQLObjectBuilder setDescription(String description)
	{
		builder.description(description);
		return this;
	}

	@Override
	public GraphQLFieldBuilder<GraphQLObjectBuilder> newField()
	{
		if(name == null)
		{
			throw context.newError(breadcrumb, "Can not add fields before type has a name");
		}

		return new GraphQLFieldBuilderImpl<GraphQLObjectBuilder>(
			context,
			code,
			breadcrumb,
			this,
			name,
			field -> {
				if(! fields.add(field.getName()))
				{
					throw context.newError("Field name `%s` is not unique", field.getName());
				}

				builder.field(field);
			}
		);
	}

	@Override
	public GraphQLObjectBuilder implement(GraphQLInterfaceType type)
	{
		builder.withInterface(type);
		return this;
	}

	@Override
	public GraphQLObjectType build()
	{
		if(type != null)
		{
			GraphQLObjectMixinEncounter encounter = new GraphQLObjectMixinEncounter()
			{

				public GraphQLFieldBuilder<?> newField()
				{
					return GraphQLObjectBuilderImpl.this.newField();
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

		return builder.build();
	}
}
