package se.l4.graphql.binding.resolver;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.InstanceFactory;
import se.l4.commons.types.conversion.TypeConverter;
import se.l4.commons.types.reflect.Annotated;
import se.l4.commons.types.reflect.MemberRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldResult;

/**
 * Context passed to bindings to perform mapping.
 */
public interface GraphQLResolverContext
{
	/**
	 * Get the current location being resolved.
	 */
	Breadcrumb getBreadcrumb();

	/**
	 * Perform the given {@Runnable} within the context of the given
	 * breadcrumb.
	 *
	 * @param crumb
	 * @param runnable
	 */
	void breadcrumb(Breadcrumb crumb, Runnable runnable);

	/**
	 * Run an action within the context of a certain breadcrumb, returning the
	 * result.
	 *
	 * @param crumb
	 * @param runnable
	 */
	<T> T breadcrumb(Breadcrumb crumb, Supplier<T> supplier);

	/**
	 * Throw an error, using the current breadcrumb as the source.
	 *
	 * @param message
	 * @param args
	 * @return
	 */
	GraphQLMappingException newError(String message);

	/**
	 * Throw an error, resolving a new breadcrumb as the error location.
	 *
	 * @param crumb
	 * @param message
	 * @param args
	 * @return
	 */
	GraphQLMappingException newError(Breadcrumb crumb, String message);

	/**
	 * Get the instance factory used to create instances of types.
	 *
	 * @return
	 */
	InstanceFactory getInstanceFactory();

	/**
	 * Get type converter being used. Can be used to convert between different
	 * primitives.
	 *
	 * @return
	 */
	TypeConverter getTypeConverter();

	/**
	 * Get the name of the type.
	 */
	String getTypeName(TypeRef type);

	/**
	 * Get if the current type name is being used.
	 *
	 * @param name
	 * @return
	 */
	boolean hasTypeName(String name);

	/**
	 * Verify that the given name is unique and request that it is reserved
	 * for our use.
	 *
	 * @param name
	 */
	void requestTypeName(String name);

	/**
	 * Get the name of a field or method.
	 *
	 * @param member
	 * @return
	 */
	String getMemberName(MemberRef member);

	/**
	 * Get the name of a parameter of a method.
	 */
	String getParameterName(ParameterRef parameter);

	/**
	 * Get the description of the given type, member or parameter.
	 *
	 * @param annotated
	 * @return
	 */
	String getDescription(Annotated annotated);

	/**
	 * Convert a Java type into a GraphQL type, throwing an exception if unable
	 * to resolve.
	 *
	 * @param type
	 * @return
	 */
	ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(TypeRef type);

	/**
	 * Convert a Java type into a GraphQL type, allowing the type to be
	 * non-existent.
	 *
	 * @param type
	 * @return
	 */
	ResolvedGraphQLType<? extends GraphQLOutputType> maybeResolveOutput(TypeRef type);

	ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(TypeRef type);

	ResolvedGraphQLType<? extends GraphQLInputType> maybeResolveInput(TypeRef type);

	/**
	 * Find all of the types that extend the given type.
	 *
	 * @param type
	 * @return
	 */
	Set<TypeRef> findExtendingTypes(TypeRef type);

	/**
	 * Find a meta annotation on the given annotated item. Meta annotations
	 * are either directly present on the annotated item, or they are present
	 * on an annotation type that then is present on the annotated item.
	 *
	 * @param <T>
	 * @param annotated
	 * @param annotation
	 * @return
	 */
	<T extends Annotation> Optional<T> findMetaAnnotation(Annotated annotated, Class<T> annotation);

	/**
	 * Apply directives defined by the given annotations to the specified
	 * field and supplier.
	 *
	 * @param annotations
	 *   annotations used to resolve directives, may include annotations that
	 *   are not directives
	 * @param field
	 *   the field to apply to
	 * @param supplier
	 *   the supplier for fetching the field values
	 * @return
	 */
	GraphQLDirectiveFieldResult applyFieldDirectives(
		Annotation[] annotations,
		GraphQLFieldDefinition field,
		DataFetchingSupplier<?> supplier
	);
}
