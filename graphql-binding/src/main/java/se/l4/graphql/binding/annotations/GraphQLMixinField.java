package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark that a method on a root object defines a field that should be mixed
 * in to another type. The first argument of the method should be annotated
 * with {@link GraphQLSource} and point to the type.
 *
 * <pre>
 * {@code @}GraphQLMixinField
 * public List<Order> getOrders({@code @}GraphQLSource Customer customer) {
 *   ...
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface GraphQLMixinField
{
}
