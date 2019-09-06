package se.l4.graphql.binding.internal.resolvers;

import java.util.OptionalDouble;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Resolver for {@link OptionalDouble}.
 */
public class OptionalDoubleResolver
	implements TypedGraphQLInputResolver, TypedGraphQLOutputResolver
{

	@Override
	public Class<?> getType()
	{
		return OptionalDouble.class;
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(Scalars.GraphQLFloat)
			.withOutputConversion((env, s) -> {
				OptionalDouble argument = (OptionalDouble) s;
				return argument.isPresent() ? argument.getAsDouble() : null;
			});
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(Scalars.GraphQLFloat)
			.withInputConversion((env, s) -> s == null ? OptionalDouble.empty() : OptionalDouble.of(((Number) s).doubleValue()))
			.withDefaultValue(env -> OptionalDouble.empty());
	}

	@Override
	public String toString()
	{
		return "OptionalDouble scalar";
	}
}
