package se.l4.graphql.binding.resolver.input;

import graphql.schema.GraphQLInputType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;

/**
 * Resolver responsible for creating a {@link GraphQLInputType} for a certain
 * Java type.
 */
public interface GraphQLInputResolver
	extends GraphQLResolver
{
	/**
	 * Perform a check if this input resolver might support the given type.
	 * This method is expected to perform a minimal check, such as for the
	 * {@link TypeRef#getErasedType()} and annotations on the type. More
	 * detailed checks should be done in {@link #resolveInput(GraphQLOutputEncounter)}.
	 *
	 * @return
	 */
	boolean supportsInput(TypeRef type);

	/**
	 * Resolve the {@link GraphQLInputType} of the given type.
	 *
	 * @param encounter
	 * @return
	 */
	ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter);
}
