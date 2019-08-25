package se.l4.graphql.binding.internal;

import graphql.schema.DataFetchingEnvironment;

/**
 * Supplier that provides a value based on the current
 * {@link DataFetchingEnvironment}.
 *
 * @param <T>
 */
public interface DataFetchingSupplier<T>
{
	T get(DataFetchingEnvironment environment);
}
