package se.l4.graphql.binding.resolver;

/**
 * Mapping used for interfaces and unions to give the binder hints about which
 * types can be converted into GraphQL types.
 *
 * @param <I>
 *   the input type
 * @param <O>
 *   the output type
 */
public interface GraphQLInterfaceMapping<I, O>
	extends GraphQLResolver
{
}
