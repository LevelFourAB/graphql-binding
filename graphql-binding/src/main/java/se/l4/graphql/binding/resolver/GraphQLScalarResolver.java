package se.l4.graphql.binding.resolver;

/**
 * Interface that can be used to implement a custom GraphQL scalar.
 *
 * @param <PrimitiveType>
 * @param <Type>
 */
public interface GraphQLScalarResolver<Type, GraphQLType>
	extends GraphQLResolver
{
	/**
	 * Serialize the specified instance into a GraphQL type.
	 *
	 * @param instance
	 * @return
	 */
	GraphQLType serialize(Type instance);

	/**
	 * Parse a value turning it into the type.
	 *
	 * @param input
	 * @return
	 */
	Type parseValue(GraphQLType input);
}
