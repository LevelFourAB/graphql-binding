package se.l4.graphql.binding.resolver.output;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;

/**
 * Resolver responsible for creating a {@link GraphQLOutputType} for a certain
 * Java type.
 */
public interface GraphQLOutputResolver
	extends GraphQLResolver
{
	/**
	 * Perform a check if this output resolver might support the given type.
	 * This method is expected to perform a minimal check, such as for the
	 * {@link TypeRef#getErasedType()} and annotations on the type. More
	 * detailed checks should be done in {@link #resolveOutput(GraphQLOutputEncounter)}.
	 *
	 * @return
	 */
	boolean supportsOutput(TypeRef type);

	/**
	 * Resolve the {@link GraphQLType} of the given type.
	 *
	 * @param encounter
	 * @return
	 */
	ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter);
}
