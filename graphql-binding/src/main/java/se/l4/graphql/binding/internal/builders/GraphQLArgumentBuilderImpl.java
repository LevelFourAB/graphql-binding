package se.l4.graphql.binding.internal.builders;

import java.util.function.Consumer;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.query.GraphQLArgumentBuilder;

public class GraphQLArgumentBuilderImpl<Parent>
	implements GraphQLArgumentBuilder<Parent>
{
	private final GraphQLResolverContext context;
	private final Breadcrumb breadcrumb;
	private final Parent parent;
	private final Consumer<GraphQLArgument> consumer;

	private final GraphQLArgument.Builder builder;

	private String name;
	private GraphQLInputType type;

	public GraphQLArgumentBuilderImpl(
		GraphQLResolverContext context,
		Breadcrumb breadcrumb,
		Parent parent,
		Consumer<GraphQLArgument> consumer
	)
	{
		this.context = context;
		this.breadcrumb = breadcrumb;
		this.parent = parent;
		this.consumer = consumer;

		builder = GraphQLArgument.newArgument();
	}

	@Override
	public GraphQLArgumentBuilder<Parent> over(ParameterRef parameter)
	{
		name = context.getParameterName(parameter);
		builder.name(name);
		builder.description(context.getDescription(parameter));
		return this;
	}

	@Override
	public GraphQLArgumentBuilder<Parent> setName(String name)
	{
		this.name = name;
		builder.name(name);
		return this;
	}

	@Override
	public GraphQLArgumentBuilder<Parent> setDescription(String description)
	{
		builder.description(description);
		return this;
	}

	@Override
	public GraphQLArgumentBuilder<Parent> setDefaultValue(Object defaultValue)
	{
		builder.defaultValue(defaultValue);
		return this;
	}

	@Override
	public GraphQLArgumentBuilder<Parent> setType(GraphQLInputType type)
	{
		this.type = type;
		builder.type(type);
		return this;
	}

	@Override
	public Parent done()
	{
		if(name == null || name.trim().isEmpty())
		{
			throw context.newError(breadcrumb, "Argument does not have a name");
		}

		if(type == null)
		{
			throw context.newError(breadcrumb, "Argument `" + name + "` does not have a type");
		}

		consumer.accept(builder.build());
		return parent;
	}
}
