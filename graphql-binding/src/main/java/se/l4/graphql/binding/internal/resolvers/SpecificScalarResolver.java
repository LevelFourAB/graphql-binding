package se.l4.graphql.binding.internal.resolvers;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.GraphQLScalarResolver;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Custom resolver that is registered whenever a scalar that uses
 * {@link GraphQLScalarConversion} is added.
 */
public class SpecificScalarResolver
	implements TypedGraphQLOutputResolver, TypedGraphQLInputResolver
{
	private final TypeRef javaType;
	private final TypeRef graphQLType;
	private final GraphQLScalarResolver<?, ?> scalar;

	public SpecificScalarResolver(
		TypeRef javaType,
		TypeRef graphQLType,
		GraphQLScalarResolver<?, ?> scalar
	)
	{
		this.javaType = javaType;
		this.graphQLType = graphQLType;
		this.scalar = scalar;
	}

	@Override
	public Class<?> getType()
	{
		return javaType.getErasedType();
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		return ScalarResolver.resolve(
			encounter.getContext(),
			javaType,
			graphQLType,
			scalar
		);
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		return ScalarResolver.resolve(
			encounter.getContext(),
			javaType,
			graphQLType,
			scalar
		);
	}
}
