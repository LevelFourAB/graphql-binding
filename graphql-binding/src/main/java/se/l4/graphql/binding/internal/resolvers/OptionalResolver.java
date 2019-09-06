package se.l4.graphql.binding.internal.resolvers;

import java.util.Optional;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Resolver for {@link Optional}.
 */
public class OptionalResolver
	implements TypedGraphQLInputResolver, TypedGraphQLOutputResolver
{

	@Override
	public Class<?> getType()
	{
		return Optional.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		TypeRef value = encounter.getType().getTypeParameter(0).get();
		return encounter.getContext().resolveOutput(value)
			.withOutputConversion((env, s) -> ((Optional) s).orElse(null));
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		TypeRef value = encounter.getType().getTypeParameter(0).get();
		return encounter.getContext().resolveInput(value)
			.withInputConversion((env, s) -> Optional.ofNullable(s))
			.withDefaultValue(env -> Optional.empty());
	}

	@Override
	public String toString()
	{
		return "Optional";
	}
}
