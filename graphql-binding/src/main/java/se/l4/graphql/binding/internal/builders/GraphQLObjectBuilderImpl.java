package se.l4.graphql.binding.internal.builders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixin;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixinEncounter;
import se.l4.ylem.types.reflect.TypeRef;

public class GraphQLObjectBuilderImpl
	implements GraphQLObjectBuilder
{
	private final GraphQLResolverContext context;
	private final List<GraphQLObjectMixin> mixins;

	private final GraphQLCodeRegistry.Builder code;

	private final GraphQLObjectType.Builder builder;

	private final Map<String, DataFetchingSupplier<?>> fields;

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
		this.fields = new HashMap<>();
		builder = GraphQLObjectType.newObject();
	}

	@Override
	public GraphQLObjectBuilder over(TypeRef type)
	{
		this.type = type;
		this.breadcrumb = Breadcrumb.forType(type);
		name = context.requestOutputTypeName(type);
		this.setDescription(context.getDescription(type));
		return this;
	}

	@Override
	public GraphQLObjectBuilder setName(String name)
	{
		// TODO: Verify name uniqueness
		this.name = name;
		context.requestTypeName(name);
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
		return new GraphQLFieldBuilderImpl<GraphQLObjectBuilder>(
			context,
			breadcrumb,
			this,
			(field, supplier) -> {
				if(fields.containsKey(field.getName()))
				{
					throw context.newError("Field name `" + field.getName() + "` is not unique");
				}

				if(supplier == null)
				{
					throw context.newError("Field `" + field.getName() + "` does not have a data fetcher");
				}

				fields.put(field.getName(), supplier);
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

		// Verify that a name exists
		if(name == null)
		{
			throw context.newError(breadcrumb, "No name provided for object");
		}

		// Set the name
		builder.name(name);

		// Setup all the data fetchers in the code registry
		for(Map.Entry<String, DataFetchingSupplier<?>> e : fields.entrySet())
		{
			FieldCoordinates coordinates = FieldCoordinates.coordinates(this.name, e.getKey());
			DataFetchingSupplier<?> supplier = e.getValue();
			code.dataFetcher(coordinates, (DataFetcher<?>) (env -> supplier.get(env)));
		}

		return builder.build();
	}
}
