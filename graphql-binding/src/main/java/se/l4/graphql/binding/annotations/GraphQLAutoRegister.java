package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.resolver.GraphQLResolver;

/**
 * Annotations used to mark {@link GraphQLResolver}s that should be
 * automatically loaded when using a {@link se.l4.commons.types.TypeFinder} with
 * {@link GraphQLBinder}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })

public @interface GraphQLAutoRegister
{
}
