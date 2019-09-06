package se.l4.graphql.binding.resolver;

import java.lang.annotation.Annotation;

import se.l4.graphql.binding.annotations.GraphQLContext;
import se.l4.graphql.binding.annotations.GraphQLEnvironment;

/**
 * Resolver for a specific parameter in objects and interfaces. This resolver
 * can provide extra behavior tied to an annotation. This is similar to
 * {@link GraphQLEnvironment} and {@link GraphQLContext} is implemented.
 */
public interface GraphQLParameterResolver<T extends Annotation>
	extends GraphQLResolver
{
	/**
	 * Resolve the parameter returning a supplier. This must return a supplier
	 * or throw an exception.
	 *
	 * @param encounter
	 * @return
	 */
	DataFetchingSupplier<?> resolveParameter(GraphQLParameterEncounter<T> encounter);
}
