package se.l4.graphql.binding.resolver.input;

import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.ylem.types.reflect.TypeRef;

public interface GraphQLInputEncounter
{
	/**
	 * Get the context for this encounter. Can be used to resolve names and
	 * other types.
	 *
	 * @return
	 */
	GraphQLResolverContext getContext();

	/**
	 * Get the type being resolved.
	 *
	 * @return
	 */
	TypeRef getType();
}
