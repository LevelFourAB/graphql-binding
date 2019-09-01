package se.l4.graphql.binding.resolver.directive;

import java.lang.annotation.Annotation;

public interface GraphQLDirectiveFieldResolver<A extends Annotation>
{
	/**
	 * Apply the directive to a field.
	 *
	 * @param encounter
	 */
	void applyField(GraphQLDirectiveFieldEncounter<A> encounter);
}
