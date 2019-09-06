package se.l4.graphql.binding.resolver;

import java.lang.annotation.Annotation;

import se.l4.commons.types.reflect.TypeRef;

/**
 * Encounter used together with {@link GraphQLParameterResolver}.
 *
 * @param <A>
 */
public interface GraphQLParameterEncounter<A extends Annotation>
{
	/**
	 * Get the context that can be used to resolve things such as output and
	 * input types.
	 *
	 * @return
	 */
	GraphQLResolverContext getContext();

	/**
	 * Get the annotation.
	 *
	 * @return
	 */
	A getAnnotation();

	/**
	 * Get the type of the parameter being resolved.
	 *
	 * @return
	 */
	TypeRef getType();
}
