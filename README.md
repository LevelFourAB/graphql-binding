# graphql-binding

This is a Java library for binding types and interfaces into GraphQL services
using annotations.

```java
@GraphQLType
@GraphQLName("Example")
public class ExampleService {
  
  @GraphQLField
  public String test() {
    return "example";
  }

}
```
