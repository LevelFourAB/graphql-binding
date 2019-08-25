package se.l4.graphql.binding;

/**
 * Interface that can be used to implement a custom GraphQL scalar.
 *
 * @param <PrimitiveType>
 * @param <Type>
 */
public interface GraphQLScalar<GraphQLValue, Type>
{
	/**
	 * Serialize the specified instance into a GraphQL type.
	 *
	 * @param instance
	 * @return
	 */
	GraphQLValue serialize(Type instance);

	/**
	 * Parse a value turning it into the type.
	 *
	 * @param input
	 * @return
	 */
	Type parseValue(GraphQLValue input);
}
