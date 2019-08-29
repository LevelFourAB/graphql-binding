package se.l4.graphql.binding.resolver.query;

import graphql.schema.GraphQLInterfaceType;
import se.l4.commons.types.reflect.TypeRef;

/**
 * Builder for creating GraphQL object types.
 */
public interface GraphQLInterfaceBuilder
{
	/**
	 * Set the {@link TypeRef} this object is based on. Will copy name and
	 * description from the type.
	 *
	 * @param type
	 * @return
	 */
	GraphQLInterfaceBuilder over(TypeRef type);

	/**
	 * Set the name of the object.
	 *
	 * @param name
	 * @return
	 */
	GraphQLInterfaceBuilder setName(String name);

	/**
	 * Set the description of the object.
	 */
	GraphQLInterfaceBuilder setDescription(String description);

	/**
	 * Define a new field for this object.
	 *
	 * @return
	 */
	GraphQLFieldBuilder<GraphQLInterfaceBuilder> newField();

	/**
	 * Add an implementation of this interface.
	 *
	 * @param type
	 * @return
	 */
	GraphQLInterfaceBuilder addImplementation(TypeRef type);

	/**
	 * Build the object type.
	 */
	GraphQLInterfaceType build();
}
