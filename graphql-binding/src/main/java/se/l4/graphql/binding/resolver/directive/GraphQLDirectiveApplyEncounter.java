package se.l4.graphql.binding.resolver.directive;

import graphql.schema.GraphQLDirective;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;

/**
 * Encounter used when a GraphQL is being applied to a certain field or its
 * arguments.
 */
public interface GraphQLDirectiveApplyEncounter<A>
{
	/**
	 * Get the current context.
	 *
	 * @return
	 */
	GraphQLResolverContext getContext();

	/**
	 * Get the directive as it has been defined.
	 *
	 * @return
	 */
	GraphQLDirective getDirective();

	/**
	 * Get the annotation that represents the usage.
	 *
	 * @return
	 */
	A getAnnotation();
}
