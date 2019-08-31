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
 * <p>
 * <pre>
 * {@code @}GraphQLField
 * public String list({@code @}GraphQLContext("Auth") AuthData auth) {
 * }
 * </pre>
 *
 * <p>
 * This annotation can be placed on another annotation to allow that annotation
 * to be used instead when injecting a parameter or a field:
 *
 * <p>
 * <pre>
 * {@code @}GraphQLContext("Auth")
 * {@code @}Retention(RetentionPolicy.RUNTIME)
 * {@code @}Target({ ElementType.PARAMETER, ElementType.FIELD })
 * public {@code @}interface AuthContext {
 * }
 * </pre>
 *
 * <p>
 * <pre>
 * {@code @}GraphQLField
 * public String list({@code @}AuthContext AuthData auth) {
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
