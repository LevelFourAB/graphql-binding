package se.l4.graphql.binding.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a constructor or function as a factory. Used
 * together with {@link GraphQLObject} to define automatic conversions from
 * other types.
 *
 * <p>
 * This allows a GraphQL type to be automatically created from another type,
 * like this:
 *
 * <pre>
 * class DataObject {
 *   public String id;
 * }
 *
 * {@code @}GraphQLObject
 * class DataObjectQueryType {
 *   private final DataObject object;
 *
 *   {@code @}GraphQLFactory
 *   public DataObjectQueryType({@code @}GraphQLSource DataObject source) {
 *     this.object = object;
 *   }
 *
 *   {@code @}GraphQLField
 *   public String id() {
 *     return object.id;
 *   }
 * }
 * </pre>
 *
 * Factories can also be static methods, which works for {@link GraphQLObject}
 * and {@link GraphQLEnum}:
 *
 * <pre>
 * class DataObjectQueryType {
 *   private final String id;
 *
 *   public DataObjectQueryType(String id) {
 *     this.id = id;
 *   }
 *
 *   @GraphQLField
 *   public String id() {
 *     return object.id;
 *   }
 *
 *   @GraphQLFactory
 *   public static DataObjectQueryType create(@GraphQLSource DataObject object) {
 *     return new DataObjectQueryType(object.id);
 *   }
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface GraphQLFactory
{
}
