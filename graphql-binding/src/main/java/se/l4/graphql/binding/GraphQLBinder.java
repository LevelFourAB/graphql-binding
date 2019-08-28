package se.l4.graphql.binding;

import graphql.schema.GraphQLSchema;
import se.l4.commons.types.InstanceFactory;
import se.l4.graphql.binding.internal.InternalGraphQLSchemaBuilder;

/**
 * Mapper for taking annotated classes and interfaces and turning them into
 * a {@link GraphQLSchema}.
 */
public class GraphQLBinder
{
	private final InternalGraphQLSchemaBuilder builder;

	public GraphQLBinder()
	{
		builder = new InternalGraphQLSchemaBuilder();
	}

	public GraphQLBinder withInstanceFactory(InstanceFactory factory)
	{
		builder.setInstanceFactory(factory);
		return this;
	}

	public GraphQLBinder withType(Class<?> type)
	{
		builder.addType(type);
		return this;
	}

	public GraphQLBinder withType(Class<?> type, Object resolver)
	{
		builder.bind(type, resolver);
		return this;
	}

	public GraphQLBinder withRoot(Object instance)
	{
		builder.addRootType(instance.getClass(), () -> instance);
		return this;
	}

	public <T> GraphQLBinder withScalar(Class<T> scalar, GraphQLScalar<T, ?> binding)
	{
		builder.addScalar(scalar, binding);
		return this;
	}

	/**
	 * Build a complete schema from the types.
	 *
	 * @return
	 */
	public GraphQLSchema build()
	{
		return builder.build();
	}
}
