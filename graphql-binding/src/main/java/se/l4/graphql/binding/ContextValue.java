package se.l4.graphql.binding;

import java.util.Optional;

import se.l4.graphql.binding.annotations.GraphQLContext;

/**
 * Holder for values that are from the {@link GraphQLContext}. Can be used
 * if there is a need to be able to update the context value during evaluation.
 */
public interface ContextValue<T>
{
	/**
	 * Get if the value is present.
	 *
	 * @return
	 */
	boolean isPresent();

	/**
	 * Get the current value if it is present. Will throw an error if the
	 * value is not present.
	 *
	 * @return
	 */
	T get();

	/**
	 * Get this value as an optional.
	 *
	 * @return
	 */
	Optional<T> asOptional();

	/**
	 * Update the value in the context.
	 *
	 * @param value
	 */
	void update(T value);

	/**
	 * Clear the value.
	 */
	void clear();
}
