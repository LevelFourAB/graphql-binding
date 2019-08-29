package se.l4.graphql.binding.resolver.query;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;

/**
 * Resolver responsible for creating a {@link GraphQLOutputType} for a certain
 * Java type.
 */
public interface GraphQLOutputResolver
	extends GraphQLResolver
{
	/**
	 * Resolve the {@link GraphQLType} of the given type.
	 *
	 * @param encounter
	 * @return
	 */
	ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter);
}
