package se.l4.graphql.binding.internal.directive;

import graphql.schema.GraphQLDirective;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveApplyEncounter;

/**
 * Abstract implementation of {@link GraphQLDirectiveApplyEncounter}.
 *
 * @param <A>
 */
public abstract class GraphQLDirectiveApplyEncounterImpl<A>
	implements GraphQLDirectiveApplyEncounter<A>
{
	private final GraphQLResolverContext context;
	private final GraphQLDirective directive;
	private final A annotation;

	public GraphQLDirectiveApplyEncounterImpl(
		GraphQLResolverContext context,
		GraphQLDirective directive,
		A annotation
	)
	{
		this.context = context;
		this.directive = directive;
		this.annotation = annotation;
	}

	@Override
	public GraphQLResolverContext getContext()
	{
		return context;
	}

	@Override
	public A getAnnotation()
	{
		return annotation;
	}

	@Override
	public GraphQLDirective getDirective()
	{
		return directive;
	}
}
