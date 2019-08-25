package se.l4.graphql.binding.internal.resolvers;

import java.util.Optional;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Resolver for a list.
 */
public class ListResolver
	implements GraphQLOutputResolver
{
	@Override
	public Optional<GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		GraphQLType componentType = encounter.getType().getTypeParameter(0)
			.map(encounter::resolveOutput)
			.orElseThrow(() -> new GraphQLMappingException(
				"Could not resolve a GraphQL type for `" + encounter.getType().toTypeName() + "`"
			));

		return Optional.of(
			GraphQLList.list(componentType)
		);
	}
}
