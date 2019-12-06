package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.graphql.binding.GraphQLBinder;

/**
 * Mark a field or method in a class as being a GraphQL subscription. Such a
 * field or method must return a {@link org.reactivestreams.Publisher}
 * from {@code org.org.reactivestreams}.
 *
 * This annotation can only be used in root classes added via
 * {@link GraphQLBinder#withRoot(Object)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface GraphQLSubscription
{
}
