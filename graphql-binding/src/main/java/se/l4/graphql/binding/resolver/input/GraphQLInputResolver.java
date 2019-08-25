package se.l4.graphql.binding.resolver.input;

import java.util.Optional;

import graphql.schema.GraphQLInputType;

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
	Optional<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter);
}
