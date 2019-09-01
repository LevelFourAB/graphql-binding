package se.l4.graphql.binding.resolver.directive;

import graphql.schema.GraphQLFieldDefinition;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;

/**
 * Result for an application of field directives.
 */
public class GraphQLDirectiveFieldResult
{
	private final GraphQLFieldDefinition field;
	private final DataFetchingSupplier<?> supplier;

	public GraphQLDirectiveFieldResult(
		GraphQLFieldDefinition field,
		DataFetchingSupplier<?> supplier
	)
	{
		this.field = field;
		this.supplier = supplier;
	}

	/**
	 * Get the field after all directives have been applied.
	 *
	 * @return
	 */
	public GraphQLFieldDefinition getField()
	{
		return field;
	}

	/**
	 * Get the supplier of the field value after all directives have been
	 * applied.
	 *
	 * @return
	 */
	public DataFetchingSupplier<?> getSupplier()
	{
		return supplier;
	}
}
