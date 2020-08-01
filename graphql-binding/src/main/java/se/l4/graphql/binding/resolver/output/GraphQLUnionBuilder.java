package se.l4.graphql.binding.resolver.output;

import graphql.schema.GraphQLUnionType;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Builder for creating GraphQL union types.
 */
public interface GraphQLUnionBuilder
{
	/**
	 * Set the {@link TypeRef} this union is based on. Will copy name and
	 * description from the type.
	 *
	 * @param type
	 * @return
	 */
	GraphQLUnionBuilder over(TypeRef type);

	/**
	 * Set the name of the union.
	 *
	 * @param name
	 * @return
	 */
	GraphQLUnionBuilder setName(String name);

	/**
	 * Set the description of the union.
	 */
	GraphQLUnionBuilder setDescription(String description);

	/**
	 * Add a possible type of this union.
	 *
	 * @param type
	 * @return
	 */
	GraphQLUnionBuilder addPossibleType(TypeRef type);

	/**
	 * Build the union type.
	 */
	GraphQLUnionType build();
}
