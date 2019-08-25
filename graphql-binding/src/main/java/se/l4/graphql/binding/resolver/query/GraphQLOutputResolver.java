package se.l4.graphql.binding.resolver.query;

import java.util.Optional;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

/**
 * Resolver responsible for creating a {@link GraphQLType} for a certain
 * Java type.
 */
public interface GraphQLOutputResolver
{
	/**
	 * Resolve the {@link GraphQLType} of the given type.
	 *
	 * @param encounter
	 * @return
	 */
	Optional<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter);
}
