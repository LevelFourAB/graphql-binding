package se.l4.graphql.binding.resolver.output;

import se.l4.graphql.binding.resolver.TypedGraphQLResolver;
import se.l4.ylem.types.reflect.TypeRef;

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
