package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a Java interface that should become an interface
 * within the GraphQL API. GraphQL interfaces must contain one or more fields.
 *
 * <p>
 * <pre>
 * {@code @}GraphQLInterface
 * public interface Vehicle {
 *   {@code @}GraphQLField
 *   String id();
 * }
 *
 * {@code @}GraphQLObject
 * public class Spaceship implements Vehicle {
 *   ...
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GraphQLInterface
{
}
