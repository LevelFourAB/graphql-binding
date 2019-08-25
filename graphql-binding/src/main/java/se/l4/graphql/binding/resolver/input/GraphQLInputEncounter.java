package se.l4.graphql.binding.resolver.input;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.ResolverContext;

public interface GraphQLInputEncounter
	extends ResolverContext
{
	/**
	 * Get the type being resolved.
	 *
	 * @return
	 */
	TypeRef getType();
}
