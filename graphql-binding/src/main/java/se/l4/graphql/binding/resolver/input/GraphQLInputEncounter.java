package se.l4.graphql.binding.resolver.input;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.ResolverContext;

public interface GraphQLInputEncounter
{
	/**
	 * Get the context for this encounter. Can be used to resolve names and
	 * other types.
	 *
	 * @return
	 */
	ResolverContext getContext();

	/**
	 * Get the type being resolved.
	 *
	 * @return
	 */
	TypeRef getType();
}
