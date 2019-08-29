package se.l4.graphql.binding.resolver;

import graphql.schema.DataFetchingEnvironment;

/**
 * Conversion that has access to {@link DataFetchingEnvironment}.
 *
 * @param <I>
 * @param <O>
 */
@FunctionalInterface
public interface DataFetchingConversion<I, O>
{
	/**
	 * Convert from the input type to the output type.
	 *
	 * @param environment
	 * @param object
	 * @return
	 */
	O convert(DataFetchingEnvironment environment, I object);

	/**
	 * Combine this conversion with another one, converting via this first
	 * and then via the other conversion.
	 *
	 * @param <T>
	 * @param next
	 * @return
	 */
	default <T> DataFetchingConversion<I, T> and(DataFetchingConversion<O, T> next)
	{
		return (env, object) -> next.convert(env, this.convert(env, object));
	}
}
