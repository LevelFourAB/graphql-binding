package se.l4.graphql.binding.resolver.input;

import graphql.schema.GraphQLInputType;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;

/**
 * Resolver responsible for creating a {@link GraphQLInputType} for a certain
 * Java type.
 */
public interface GraphQLInputResolver
{
	/**
	 * Resolve the {@link GraphQLInputType} of the given type.
	 *
	 * @param encounter
	 * @return
	 */
	ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter);
}
