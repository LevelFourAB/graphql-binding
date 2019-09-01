package se.l4.graphql.binding.internal.directive;

import java.util.HashMap;
import java.util.Map;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLScalarType;
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

	private final Map<String, Object> arguments;

	public GraphQLDirectiveApplyEncounterImpl(
		GraphQLResolverContext context,
		GraphQLDirective directive,
		A annotation
	)
	{
		this.context = context;
		this.directive = directive;
		this.annotation = annotation;

		this.arguments = new HashMap<>();
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

	@Override
	public void setArgument(String id, Object value)
	{
		this.arguments.put(id, value);
	}

	@Override
	public void setArguments(Map<String, Object> arguments)
	{
		this.arguments.clear();
		this.arguments.putAll(arguments);
	}

	protected GraphQLDirective buildDirective()
	{
		return directive.transform(builder -> {
			for(GraphQLArgument arg : directive.getArguments())
			{
				if(arguments.containsKey(arg.getName()))
				{
					Object value = ((GraphQLScalarType) arg.getType())
						.getCoercing()
						.parseValue(arguments.get(arg.getName()));

					builder.argument(arg.transform(
						argBuilder -> argBuilder.value(value)
					));
				}
			}
		});
	}
}
