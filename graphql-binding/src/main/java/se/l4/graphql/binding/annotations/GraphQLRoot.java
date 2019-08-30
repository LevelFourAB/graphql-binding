package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a class that should be discovered and registered as
 * a root when using {@link se.l4.graphql.binding.GraphQLBinder} together with
 * a {@link se.l4.commons.types.TypeFinder}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GraphQLRoot
{
}
