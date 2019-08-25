package se.l4.graphql.binding.resolver.query;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.MemberRef;

/**
 * Builder for a field within a GraphQL object.
 */
public interface GraphQLFieldBuilder<Parent>
{
	/**
	 * Set a {@link MemberRef} this field is based on. This will copy name,
	 * description and deprecation status from the member via annotations.
	 *
	 * @param field
	 * @return
	 */
	GraphQLFieldBuilder<Parent> over(MemberRef member);

	/**
	 * Set the name of the field.
	 *
	 * @param name
	 * @return
	 */
	GraphQLFieldBuilder<Parent> setName(String name);

	/**
	 * Set the description of the field.
	 */
	GraphQLFieldBuilder<Parent> setDescription(String description);

	/**
	 * Set if the field is deprecated.
	 */
	GraphQLFieldBuilder<Parent> setDeprecation(String deprecationReason);

	/**
	 * Set the type of this field.
	 *
	 * @param type
	 * @return
	 */
	GraphQLFieldBuilder<Parent> setType(GraphQLOutputType type);

	/**
	 * Start building a new argument for this field.
	 *
	 * @return
	 */
	GraphQLArgumentBuilder<GraphQLFieldBuilder<Parent>> newArgument();

	/**
	 * Set the data fetcher to use for the field.
	 */
	GraphQLFieldBuilder<Parent> withDataFetcher(DataFetcher<?> fetcher);

	/**
	 * Indicate that the field is done building and that it should be added
	 * to the parent type.
	 *
	 * @return
	 */
	Parent done();
}
