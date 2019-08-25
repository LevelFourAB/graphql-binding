package se.l4.graphql.binding.resolver.query;

import graphql.schema.GraphQLObjectType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.ResolverContext;

public interface GraphQLOutputMixin
{
	/**
	 * Mixin functions from the given type into the specified builder.
	 *
	 * @param context
	 * @param type
	 * @param graphqlName
	 * @param graphqlBuilder
	 */
	void mixin(
		ResolverContext context,
		TypeRef type,
		TypeRef otherType,
		String graphqlName,
		GraphQLObjectType.Builder graphqlBuilder
	);
}
