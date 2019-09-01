package se.l4.graphql.binding.resolver.directive;

import java.lang.annotation.Annotation;

import graphql.schema.GraphQLDirective;

/**
 * Resolve an annotation into a {@link GraphQLDirective}, both for the
 * definition and the usage.
 *
 * @param <A>
 */
public interface GraphQLDirectiveResolver<A extends Annotation>
{
	/**
	 * Create the directive.
	 *
	 * @param encounter
	 * @return
	 */
	GraphQLDirective createDirective(GraphQLDirectiveCreationEncounter encounter);
}
