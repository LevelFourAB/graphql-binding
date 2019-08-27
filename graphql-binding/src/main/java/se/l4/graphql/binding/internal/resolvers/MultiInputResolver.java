package se.l4.graphql.binding.internal.resolvers;

import java.util.List;

import graphql.schema.GraphQLInputType;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;

public class MultiInputResolver
	implements GraphQLInputResolver
{
	private final List<GraphQLInputResolver> resolvers;

	public MultiInputResolver(List<GraphQLInputResolver> resolvers)
	{
		this.resolvers = resolvers;
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		for(GraphQLInputResolver resolver : resolvers)
		{
			ResolvedGraphQLType<? extends GraphQLInputType> resolved = resolver.resolveInput(encounter);
			if(resolved.isPresent())
			{
				return resolved;
			}
		}

		return ResolvedGraphQLType.none();
	}
}
