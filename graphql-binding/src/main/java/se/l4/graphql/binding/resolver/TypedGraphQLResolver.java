package se.l4.graphql.binding.resolver;

/**
 * Extension to {@link GraphQLResolver} used when a resolver requests to be
 * bound only to a single type.
 */
public interface TypedGraphQLResolver
	extends GraphQLResolver
{
	/**
	 * Get the type this resolver works on.
	 *
	 * @return
	 */
	Class<?> getType();
}
