package se.l4.graphql.binding.resolver.input;

import se.l4.graphql.binding.resolver.TypedGraphQLResolver;
import se.l4.ylem.types.reflect.TypeRef;

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
