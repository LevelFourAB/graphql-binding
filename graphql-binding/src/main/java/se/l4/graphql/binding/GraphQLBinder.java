package se.l4.graphql.binding;

import java.util.HashSet;
import java.util.Set;

import graphql.schema.GraphQLSchema;

/**
 * Mapper for taking annotated classes and interfaces and turning them into
 * a {@link GraphQLSchema}.
 */
public class GraphQLBinder
{
	private final Set<Class<?>> types;

	public GraphQLBinder()
	{
		types = new HashSet<>();
	}

	public GraphQLBinder withType(Class<?> type)
	{
		types.add(type);
		return this;
	}

	/**
	 * Build a complete schema from the types.
	 *
	 * @return
	 */
	public GraphQLSchema build()
	{
		throw new UnsupportedOperationException();
	}
}
