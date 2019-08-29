package se.l4.graphql.binding.internal.builders;

import java.util.HashSet;
import java.util.Set;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.query.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLObjectBuilder;

public class GraphQLObjectBuilderImpl
	implements GraphQLObjectBuilder
{
	private final GraphQLResolverContext context;

	private final GraphQLCodeRegistry.Builder code;
	private final GraphQLObjectType.Builder builder;

	private final Set<String> fields;

	private Breadcrumb breadcrumb;

	private String name;

	public GraphQLObjectBuilderImpl(
		GraphQLResolverContext context,
		GraphQLCodeRegistry.Builder code
	)
	{
		this.code = code;
		this.context = context;

		breadcrumb = Breadcrumb.empty();
		this.fields = new HashSet<>();
		builder = GraphQLObjectType.newObject();
	}

	@Override
	public GraphQLObjectBuilder over(TypeRef type)
	{
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
		return builder.build();
	}
}
