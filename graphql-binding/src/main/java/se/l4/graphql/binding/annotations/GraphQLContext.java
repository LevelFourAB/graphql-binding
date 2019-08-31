package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used on a parameter within a method annotated with
 * {@link GraphQLField} to access something stored in the global context for
 * the current execution. The use of this annotation assumes that the global
 * context has been set up to be an instance of {@link graphql.GraphQLContext}.
 *
 * <pre>
 * @GraphQLField
 * public String list(@GraphQLContext("Auth") AuthData auth) {
 * }
 * </pre>
 *
 * This annotation can be placed on another annotation to allow that annotation
 * to be used instead when injecting a parameter or a field:
 *
 * <pre>
 * @GraphQLContext("Auth")
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target({ ElementType.PARAMETER, ElementType.FIELD })
 * public @interface AuthContext {
 * }
 * </pre>
 *
 * <pre>
 * @GraphQLField
 * public String list(@AuthContext AuthData auth) {
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface GraphQLContext
{
	/**
	 * The context key to get.
	 */
	String value();
}
