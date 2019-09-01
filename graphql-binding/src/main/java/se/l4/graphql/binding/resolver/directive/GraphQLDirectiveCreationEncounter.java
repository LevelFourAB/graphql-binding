package se.l4.graphql.binding.resolver.directive;

import se.l4.graphql.binding.resolver.GraphQLResolverContext;

/**
 * Encounter for when the definition of a GraphQL Directive is being created.
 */
public interface GraphQLDirectiveCreationEncounter
{
	/**
	 * Get the current context.
	 *
	 * @return
	 */
	GraphQLResolverContext getContext();
}
