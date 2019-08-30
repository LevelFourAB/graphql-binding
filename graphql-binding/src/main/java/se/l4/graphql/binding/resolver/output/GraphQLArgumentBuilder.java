package se.l4.graphql.binding.resolver.output;

import graphql.schema.GraphQLInputType;
import se.l4.commons.types.reflect.ParameterRef;

/**
 * Builder for an argument within a {@link GraphQLFieldBuilder}.
 *
 * @param <Parent>
 */
public interface GraphQLArgumentBuilder<Parent>
{
	/**
	 * Set the {@link ParameterRef} this argument is based on. This will copy
	 * the name and description from the parameter via annotations.
	 *
	 * @param parameter
	 * @return
	 */
	GraphQLArgumentBuilder<Parent> over(ParameterRef parameter);

	/**
	 * Set the name of the argument.
	 *
	 * @param name
	 * @return
	 */
	GraphQLArgumentBuilder<Parent> setName(String name);

	/**
	 * Set the description of the argument.
	 *
	 * @return
	 */
	GraphQLArgumentBuilder<Parent> setDescription(String description);

	/**
	 * Set the input type this argument uses.
	 *
	 * @return
	 */
	GraphQLArgumentBuilder<Parent> setType(GraphQLInputType type);

	/**
	 * Set the default value for this argument.
	 *
	 * @param defaultValue
	 * @return
	 */
	GraphQLArgumentBuilder<Parent> setDefaultValue(Object defaultValue);

	/**
	 * Indicate that we are done building this argument.
	 *
	 * @return
	 */
	Parent done();
}
