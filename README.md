# graphql-binding

![Version](https://img.shields.io/maven-central/v/se.l4.graphql.binding/graphql-binding)
![Dependencies](https://github.com/levelfourab/graphql-binding/workflows/CI/badge.svg)

This is a Java library for binding types and interfaces into GraphQL services
using annotations for use with [graphql-java](https://github.com/graphql-java/graphql-java).

```java
@GraphQLObject
@GraphQLName("Example")
public class ExampleService {
  
  @GraphQLField
  public String test() {
    return "example";
  }

}
```

## Features

* Code-first approach to creating GraphQL types via annotations
* Explicit bindings, no bindings of anything unless annotated, manually resolved or a scalar
* Support for object types, enums, interfaces and input types
* Mixins for object types, allowing GraphQL types to be extended
* Conversion of objects into GraphQL types
* Automatic type discovery using an instance of `TypeFinder`
* Integration with Dependency Injection via `InstanceFactory`

## License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0),
see the file `LICENSE` and `NOTICE` for details.

## Usage via Maven

This library are available from Maven central:

```xml
<dependency>
  <groupId>se.l4.graphql.binding</groupId>
  <artifactId>grapqhl-binding</artifactId>
  <version>3.0.0</version>
</dependency>
```

## Creating a `GraphQLSchema`

The `GraphQLSchema` for use with other GraphQL Java tooling, such as 
[graphql-java-servlet](https://github.com/graphql-java-kickstart/graphql-java-servlet) is
created via the type `GraphQLBinder`.

```java
GraphQLSchema schema = GraphQLBinder.newBinder()
  .withRoot(new RootObject())
  .build();
```

### Setting an instance factory

This library uses an `InstanceFactory` to resolve non-GraphQL instances, the
default version can create objects with default constructors. Guice integration
is available via an `InstanceFactory` from [`commons-guice`](https://github.com/levelfourab/commons).

```java
binder.setInstanceFactory(factory);
```

### Automatic discovery of types

Types can be automatically discovered using a `TypeFinder` instance. If a
`TypeFinder` is provided it will be queried for types that are annotated with
`@GraphQLObject`, `@GraphQLEnum`, `@GraphQLInterface` and `@GraphQLInputObject`.
Types annotated with `@GraphQLRoot` will be created via the current 
`InstanceFactory` and added as root objects.

```java
binder.setTypeFinder(TypeFinder.builder()
  .setInstanceFactory(instanceFactory) // if using a custom instance factory
  .addPackage("root.package.to.scan")
  .build()
)
```

## Defining a GraphQL object

GraphQL object types are created via the `@GraphQLObject` annotation and
placing `@GraphQLField` on public fields and methods to make them part of the
object.

```java
@GraphQLObject
public class Pet {
  @GraphQLField
  public final String name;

  public Pet(String name) {
    this.name = name;
  }

  @GraphQLField
  public String fieldViaMethod(
    @GraphQLName("argumentName") String argument
  ) {
    return argument;
  }
}
```

If a field can not return `null` it can be annotated with  `@GraphQLNonNull` 
to indicate so in the schema. The same is true for arguments. Some types such
as `List` can also have annotations placed on their inner type:

```java
@GraphQLField
public List<@GraphQLNonNull String> list() {
  ...
}
```

## Defining a GraphQL enum

Enumerations can be defined as regular enums with the annotation `@GraphQLEnum`.
Both the enum class and individual values in the enum can be annotated with
`@GraphQLName` and `@GraphQLDescription`.

```java
@GraphQLEnum
public enum PetType {
  DOG,

  @GraphQLDescription("This is a cat")
  CAT;
}
```

## Query and Mutations via root objects

Root queries and mutations are registered via root objects. These can either
be added via `binder.withRoot(instance)` or when using type finding by
annotating a type with `@GraphQLRoot`. Several root objects may exist at the
same time and will all contribute to the initial `Query` and `Mutation` types.

```java
public class RootObject {
  @GraphQLField
  public String test() {
    return "Hello World";
  }
}

binder.withRoot(new GraphQLRootTest());
```

The above root type would expose a single field named `test`:

```graphql
query {
  test
}
```

Use `@GraphQLMutation` to define mutations:

```java
public class RootObject {
  @GraphQLMutation
  public String createThing(
    @GraphQLName("input") String name
  ) {
    ...
  }
}
```

## Conversion of objects

This library supports the conversion from a non-GraphQL type into a GraphQL
type. This allows one type to return say a `Customer` and have it mapped into
a `CustomerQueryType`:

```java
@GraphQLObject
public class CustomersQueryType {
  @GraphQLField
  public Customer getById(String id) {
    // This method can look up and return an instance of Customer
    return ...;
  }
}

@GraphQLObject
@GraphQLName("Customer")
public class CustomerQueryType {
  private final Customer customer;

  @GraphQLFactory
  public CustomerQueryType(@GraphQLSource Customer customer) {
    this.customer = customer;
  }

  @GraphQLField
  @GraphQLDescription("The identifier of the customer")
  public String id() {
    return customer.getId();
  }
}
```

Add the type to the binder to allow it to register the conversion:

```java
binder.withType(CustomerQueryType.class);
```

## Mixins

Root objects can be used to extend other object types using mixins. This is
useful if one of your modules want to extend a type defined by another module.
Adding `@GraphQLMixinField` to a method allows it to extend the type defined
by a parameter annotated with `@GraphQLSource`:

```java
public class RootObject {
  @GraphQLMixinField
  public int extendedMethod(
    @GraphQLSource GraphQLObject source,
    @GraphQLName("argumentName") String argument
  ) {
    ...
  }
}
```
