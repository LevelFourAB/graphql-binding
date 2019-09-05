package se.l4.graphql.binding.internal.factory;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.DataFetchingConversion;

/**
 * Factory that can create an object based on the current environment and a
 * source.
 *
 * @param <I>
 * @param <O>
 */
public interface Factory<I, O>
	extends DataFetchingConversion<I, O>
{
	/**
	 * Get the type of input this factory takes.
	 *
	 * @return
	 */
	TypeRef getInput();

	/**
	 * Get the type of object this factory creates.
	 *
	 * @return
	 */
	TypeRef getOutput();
}
