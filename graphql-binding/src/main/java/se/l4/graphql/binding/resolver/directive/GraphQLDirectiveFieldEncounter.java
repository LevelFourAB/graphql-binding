package se.l4.graphql.binding.resolver.directive;

import java.util.function.Consumer;

import graphql.schema.GraphQLFieldDefinition;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;

public interface GraphQLDirectiveFieldEncounter<A>
	extends GraphQLDirectiveApplyEncounter<A>
{
	/**
	 * Get the field on which the directive is being applied.
	 *
	 * @return
	 */
	GraphQLFieldDefinition getField();

	/**
	 * Transform the field using the given builder.
	 *
	 * @param consumer
	 */
	void transformField(Consumer<GraphQLFieldDefinition.Builder> consumer);

	/**
	 * Get the supplier currently used.
	 *
	 * @return
	 */
	DataFetchingSupplier<?> getSupplier();

	/**
	 * Update the supplier.
	 *
	 * @param supplier
	 */
	void setSupplier(DataFetchingSupplier<?> supplier);
}
