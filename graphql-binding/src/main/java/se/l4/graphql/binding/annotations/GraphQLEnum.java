package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark an enum. This annotation can be placed on a Java
 * enum to make it usable as a GraphQL type.
 *
 * <p>
 * <pre>
 * {@code @}GraphQLEnum
 * public enum Test {
 *   VALUE,
 *
 *   {@code @}GraphQLName("RENAMED")
 *   RENAMED_VALUE;
 * }
 * </pre>
 *
 * <p>
 * Specific annotation values can be named and described by placing annotations
 * on their declarations. See {@link GraphQLName} and
 * {@link GraphQLDescription}.
 *
 * <p>
 * Conversion from another enumeration can be performed via a static
 * factory:
 *
 * <p>
 * <pre>
 * public enum Test {
 *   VALUE,
 *
 *   {@code @}GraphQLName("RENAMED")
 *   RENAMED_VALUE;
 *
 *   {@code @}GraphQLFactory
 *   public static Test resolve({@code @}GraphQLSource OtherEnum other) {
 *      switch(other) {
 *         case VALUE:
 *           return Test.VALUE;
 *         ...
 *      }
 *
 *      throw new AssertionError("Unsupported: " + other);
 *   }
 * }
 * </pre>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GraphQLEnum
{
}
