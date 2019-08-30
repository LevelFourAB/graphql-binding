package se.l4.graphql.binding;

import graphql.schema.GraphQLSchema;
import se.l4.commons.types.InstanceFactory;
import se.l4.commons.types.TypeFinder;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.internal.InternalGraphQLSchemaBuilder;
import se.l4.graphql.binding.resolver.GraphQLResolver;

/**
 * Mapper for taking annotated classes and interfaces and turning them into
 * a {@link GraphQLSchema}.
 */
public class GraphQLBinder
{
	private final InternalGraphQLSchemaBuilder builder;

	private TypeFinder typeFinder;

	public GraphQLBinder()
	{
		builder = new InternalGraphQLSchemaBuilder();
	}

	public GraphQLBinder withInstanceFactory(InstanceFactory factory)
	{
		builder.setInstanceFactory(factory);
		return this;
	}

	public GraphQLBinder withTypeFinder(TypeFinder finder)
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

	public <T> GraphQLBinder withScalar(Class<T> scalar, GraphQLScalar<T, ?> binding)
	{
		builder.addScalar(scalar, binding);
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
		}

		return builder.build();
	}
}
