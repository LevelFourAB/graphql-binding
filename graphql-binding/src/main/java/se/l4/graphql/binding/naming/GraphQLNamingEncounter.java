package se.l4.graphql.binding.naming;

import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Encounter used when naming types.
 */
public interface GraphQLNamingEncounter
{
	/**
	 * Get the current context.
	 *
	 * @return
	 */
	GraphQLResolverContext getContext();

	/**
	 * Get if this is an output type being named.
	 *
	 * @return
	 */
	boolean isOutput();

	/**
	 * Get if this is an input type being named.
	 *
	 * @return
	 */
	boolean isInput();

	/**
	 * Get the type being named.
	 *
	 * @return
	 */
	TypeRef getType();
}
