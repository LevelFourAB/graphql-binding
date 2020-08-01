package se.l4.graphql.binding.resolver.output;

import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Encounter used together with {@link GraphQLObjectMixin}.
 */
public interface GraphQLObjectMixinEncounter
{
	/**
	 * Get the context for this encounter. Can be used to resolve to other
	 * input and output types as needed.
	 *
	 * @return
	 */
	GraphQLResolverContext getContext();

	/**
	 * Get the type to mixin for.
	 *
	 * @return
	 */
	TypeRef getType();

	/**
	 * Add a new field to the current output object.
	 *
	 * @return
	 */
	GraphQLFieldBuilder<?> newField();
}
