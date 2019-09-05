package se.l4.graphql.binding.resolver;

import se.l4.graphql.binding.annotations.GraphQLObject;

/**
 * Conversion between one type to another. This can be used to simplify
 * conversion of an object into a GraphQL representation.
 *
 * Conversions may convert directly into a type that has a GraphQL
 * representation, such as those with a {@link GraphQLObject} annotation. But
 * they can also convert into another type, which in turn may be converted
 * until a GraphQL type is found.
 *
 * @param <I>
 *   the input type
 * @param <O>
 *   the output type
 */
public interface GraphQLConversion<I, O>
	extends GraphQLResolver, DataFetchingConversion<I, O>
{
}
