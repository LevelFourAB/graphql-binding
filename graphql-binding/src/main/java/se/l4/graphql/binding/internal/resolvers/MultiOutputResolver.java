package se.l4.graphql.binding.internal.resolvers;

import java.util.List;
import java.util.Optional;

import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

public class MultiOutputResolver
	implements GraphQLOutputResolver
{
	private final List<GraphQLOutputResolver> resolvers;

	public MultiOutputResolver(List<GraphQLOutputResolver> resolvers)
	{
		this.resolvers = resolvers;
	}

	@Override
	public Optional<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		for(GraphQLOutputResolver resolver : resolvers)
		{
			Optional<? extends GraphQLOutputType> resolved = resolver.resolveOutput(encounter);
			if(resolved.isPresent())
			{
				return resolved;
			}
		}

		return Optional.empty();
	}
}
