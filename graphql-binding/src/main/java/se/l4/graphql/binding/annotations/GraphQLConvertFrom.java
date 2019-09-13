package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hint about which Java interfaces represent this GraphQL interface or union.
 * This is used to generate a conversion from the interfaces into this GraphQL
 * type by other conversions that can be converted into something that matches
 * this GraphQL type.
 *
 * <p>
 * <pre>
 * {@literal @}GraphQLInterface
 * {@literal @}GraphQLConvertFrom(JavaExampleInterface.class)
 * interface GraphQLExampleInterface {
 *   ...
 * }
 *
 * interface JavaExampleInterface {
 *   ...
 * }
 *
 * {@literal @}GraphQLObject
 * class GraphQLExampleObject implements GraphQLExampleInterface {
 *   {@literal @}GraphQLFactory
 *   public GraphQLExampleType({@literal @}GraphQLSource JavaExampleObject source) {
 *     ...
 *   }
 * }
 *
 * class JavaExampleObject implements JavaExampleInterface {
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GraphQLConvertFrom
{
	/**
	 * Classes that convert into this GraphQL interface or union.
	 *
	 * @return
	 */
	Class<?>[] value();
}
