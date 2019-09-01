package se.l4.graphql.binding.internal.directive;

import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveCreationEncounter;

/**
 * Implementation of {@link GraphQLDirectiveCreationEncounter}.
 */
public class GraphQLDirectiveCreationEncounterImpl
	implements GraphQLDirectiveCreationEncounter
{
	private final GraphQLResolverContext context;

	public GraphQLDirectiveCreationEncounterImpl(
		GraphQLResolverContext context
	)
	{
		this.context = context;
	}

	@Override
	public GraphQLResolverContext getContext()
	{
		return context;
	}

}
