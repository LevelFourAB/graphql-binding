package se.l4.graphql.binding.internal.resolvers;

import java.util.OptionalLong;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Resolver for {@link OptionalLong}.
 */
public class OptionalLongResolver
	implements TypedGraphQLInputResolver, TypedGraphQLOutputResolver
{

	@Override
	public Class<?> getType()
	{
		return OptionalLong.class;
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(Scalars.GraphQLLong)
			.withOutputConversion((env, s) -> {
				OptionalLong argument = (OptionalLong) s;
				return argument.isPresent() ? argument.getAsLong() : null;
			});
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(Scalars.GraphQLLong)
			.withInputConversion((env, s) -> s == null ? OptionalLong.empty() : OptionalLong.of((Long) s))
			.withDefaultValue(env -> OptionalLong.empty());
	}

	@Override
	public String toString()
	{
		return "OptionalLong scalar";
	}
}
