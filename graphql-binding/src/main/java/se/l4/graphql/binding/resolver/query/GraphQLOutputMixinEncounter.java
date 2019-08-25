package se.l4.graphql.binding.resolver.query;

import se.l4.commons.types.reflect.TypeRef;

public interface GraphQLOutputMixinEncounter
{
	/**
	 * Get the type mixins are being created for.
	 *
	 * @return
	 */
	TypeRef getType();


}
