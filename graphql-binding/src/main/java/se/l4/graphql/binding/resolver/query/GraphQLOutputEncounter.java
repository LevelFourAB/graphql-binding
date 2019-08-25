package se.l4.graphql.binding.resolver.query;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.ResolverContext;

public interface GraphQLOutputEncounter
	extends ResolverContext
{
	/**
	 * Get the type being resolved.
	 *
	 * @return
	 */
	TypeRef getType();

	/**
	 * Start building a new object type.
	 */
	GraphQLObjectBuilder newObjectType();
}
