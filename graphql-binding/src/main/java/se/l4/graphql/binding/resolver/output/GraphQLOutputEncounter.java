package se.l4.graphql.binding.resolver.output;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;

public interface GraphQLOutputEncounter
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

	/**
	 * Start building a new object type.
	 *
	 * @return
	 */
	GraphQLObjectBuilder newObjectType();

	/**
	 * Start building a new interface type.
	 *
	 * @return
	 */
	GraphQLInterfaceBuilder newInterfaceType();

	/**
	 * Start building a new union type.
	 *
	 * @return
	 */
	GraphQLUnionBuilder newUnionType();
}
