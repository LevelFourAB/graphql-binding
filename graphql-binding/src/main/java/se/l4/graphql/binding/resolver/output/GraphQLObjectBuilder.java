package se.l4.graphql.binding.resolver.output;

import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Builder for creating GraphQL object types.
 */
public interface GraphQLObjectBuilder
{
	/**
	 * Set the {@link TypeRef} this object is based on. Will copy name and
	 * description from the type.
	 *
	 * @param type
	 * @return
	 */
	GraphQLObjectBuilder over(TypeRef type);

	/**
	 * Set the name of the object.
	 *
	 * @param name
	 * @return
	 */
	GraphQLObjectBuilder setName(String name);

	/**
	 * Set the description of the object.
	 */
	GraphQLObjectBuilder setDescription(String description);

	/**
	 * Define a new field for this object.
	 *
	 * @return
	 */
	GraphQLFieldBuilder<GraphQLObjectBuilder> newField();

	/**
	 * Indicate that this type implements the given interface.
	 *
	 * @return
	 */
	GraphQLObjectBuilder implement(GraphQLInterfaceType type);

	/**
	 * Build the object type.
	 */
	GraphQLObjectType build();
}
