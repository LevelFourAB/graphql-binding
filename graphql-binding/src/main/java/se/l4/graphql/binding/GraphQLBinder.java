package se.l4.graphql.binding;

import java.lang.annotation.Annotation;

import graphql.schema.GraphQLSchema;
import se.l4.commons.types.InstanceFactory;
import se.l4.commons.types.TypeFinder;
import se.l4.graphql.binding.annotations.GraphQLEnum;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLRoot;
import se.l4.graphql.binding.internal.InternalGraphQLSchemaBuilder;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.GraphQLScalarResolver;

/**
 * Mapper for taking annotated classes and interfaces and turning them into
 * a {@link GraphQLSchema}.
 */
public class GraphQLBinder
{
	private final InternalGraphQLSchemaBuilder builder;

	private TypeFinder typeFinder;

	private GraphQLBinder()
	{
		builder = new InternalGraphQLSchemaBuilder();
	}

	public static GraphQLBinder newBinder()
	{
		return new GraphQLBinder();
	}

	public GraphQLBinder setInstanceFactory(InstanceFactory factory)
	{
		builder.setInstanceFactory(factory);
		return this;
	}

	public GraphQLBinder setTypeFinder(TypeFinder finder)
	{
		this.typeFinder = finder;
		return this;
	}

	public GraphQLBinder withType(Class<?> type)
	{
		builder.addType(type);
		return this;
	}

	public GraphQLBinder withResolver(GraphQLResolver resolver)
	{
		builder.addResolver(resolver);
		return this;
	}

	public GraphQLBinder withRoot(Object instance)
	{
		builder.addRootType(instance.getClass(), (env) -> instance);
		return this;
	}

	public <T> GraphQLBinder withScalar(GraphQLScalarResolver<T, ?> binding)
	{
		builder.addScalar(binding);
		return this;
	}
		return this;
	}

	/**
	 * Build a complete schema from the types.
	 *
	 * @return
	 */
	public GraphQLSchema build()
	{
		if(typeFinder != null)
		{
			for(Class<?> c : typeFinder.getTypesAnnotatedWith(GraphQLObject.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeFinder.getTypesAnnotatedWith(GraphQLInputObject.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeFinder.getTypesAnnotatedWith(GraphQLEnum.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeFinder.getTypesAnnotatedWith(GraphQLInterface.class))
			{
				builder.addType(c);
			}

			for(Object instance : typeFinder.getTypesAnnotatedWithAsInstances(GraphQLRoot.class))
			{
				builder.addRootType(instance.getClass(), env -> instance);
			}
		}

		return builder.build();
	}
}
