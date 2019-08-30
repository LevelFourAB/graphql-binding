package se.l4.graphql.binding.resolver.query;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.TypedGraphQLResolver;

/**
 * Extension to {@link GraphQLOutputResolver} that acts only on a certain type.
 */
public interface TypedGraphQLOutputResolver
	extends GraphQLOutputResolver, TypedGraphQLResolver
{
	@Override
	default boolean supportsOutput(TypeRef type)
	{
		return type.getErasedType() == getType();
	}
}
