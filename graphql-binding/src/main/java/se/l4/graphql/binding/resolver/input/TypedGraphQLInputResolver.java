package se.l4.graphql.binding.resolver.input;

import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.TypedGraphQLResolver;

/**
 * Extension to {@link GraphQLInputResolver} that acts only on a certain type.
 */
public interface TypedGraphQLInputResolver
	extends GraphQLInputResolver, TypedGraphQLResolver
{
	@Override
	default boolean supportsInput(TypeRef type)
	{
		return type.getErasedType() == getType();
	}
}
