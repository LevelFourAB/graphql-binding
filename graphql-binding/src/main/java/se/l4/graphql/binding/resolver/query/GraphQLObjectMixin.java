package se.l4.graphql.binding.resolver.query;

import se.l4.commons.types.reflect.TypeRef;

/**
 * Mixin functionality for GraphQL objects. Allows for adding some extra fields
 * when an object is being built. The primary use is to allow for root objects
 * to extend other types via annotations.
 */
public interface GraphQLObjectMixin
{
	/**
	 * Get if this mixin supports the given type.
	 *
	 * @param type
	 * @return
	 */
	boolean supportsOutputMixin(TypeRef type);

	/**
	 * Perform a mixin for the object being built.
	 *
	 * @param encounter
	 */
	void mixin(GraphQLObjectMixinEncounter encounter);
}
