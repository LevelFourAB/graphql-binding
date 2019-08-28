package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to specify a source when creating a GraphQL type
 * either via a constructor or a static factory method.
 *
 * <pre>
 * public CustomType(@GraphQLSource SourceType source) {
 * }
 *
 * public static CustomType create(@GraphQLSource SourceType source) {
 *   ...
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface GraphQLSource
{
}
