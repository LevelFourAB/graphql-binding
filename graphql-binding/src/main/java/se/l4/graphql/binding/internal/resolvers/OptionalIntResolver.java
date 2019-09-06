package se.l4.graphql.binding.internal.resolvers;

import java.util.OptionalInt;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Resolver for {@link OptionalInt}.
 */
public class OptionalIntResolver
	implements TypedGraphQLInputResolver, TypedGraphQLOutputResolver
{

	@Override
	public Class<?> getType()
	{
		return OptionalInt.class;
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(Scalars.GraphQLInt)
			.withOutputConversion((env, s) -> {
				OptionalInt argument = (OptionalInt) s;
				return argument.isPresent() ? argument.getAsInt() : null;
			});
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(Scalars.GraphQLInt)
			.withInputConversion((env, s) -> s == null ? OptionalInt.empty() : OptionalInt.of((Integer) s))
			.withDefaultValue(env -> OptionalInt.empty());
	}

	@Override
	public String toString()
	{
		return "OptionalInt scalar";
	}
}
