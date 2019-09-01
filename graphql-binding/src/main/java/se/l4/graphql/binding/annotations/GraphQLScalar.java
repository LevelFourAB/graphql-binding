package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.resolver.GraphQLScalarResolver;

/**
 * Annotation used to mark a class or interface that should become a scalar
 * type. This is equivalent to registering the scalar via
 * {@link GraphQLBinder#withScalar(Class, GraphQLScalarResolver)}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GraphQLScalar
{
	/**
	 * The resolver to use for this scalar. This will be automatically
	 * constructed and applied for this scalar.
	 */
	Class<? extends GraphQLScalarResolver<?, ?>> value();
}
