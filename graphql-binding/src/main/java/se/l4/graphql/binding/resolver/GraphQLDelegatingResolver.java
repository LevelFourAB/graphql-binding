package se.l4.graphql.binding.resolver;

/**
 * Marker interface used to mark any resolvers that delegate their work to
 * another resolver. This is used for conversions and for types that do not
 * have any names, such as lists.
 */
public interface GraphQLDelegatingResolver
{

}
