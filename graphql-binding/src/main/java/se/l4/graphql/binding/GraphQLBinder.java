package se.l4.graphql.binding;

import java.lang.annotation.Annotation;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;
import graphql.schema.GraphQLSchema;
import se.l4.graphql.binding.annotations.GraphQLAutoRegister;
import se.l4.graphql.binding.annotations.GraphQLEnum;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLRoot;
import se.l4.graphql.binding.annotations.GraphQLUnion;
import se.l4.graphql.binding.internal.InternalGraphQLSchemaBuilder;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveResolver;
import se.l4.ylem.types.discovery.TypeDiscovery;
import se.l4.ylem.types.instances.InstanceFactory;

/**
 * Mapper for taking annotated classes and interfaces and turning them into
 * a {@link GraphQLSchema}.
 */
public class GraphQLBinder
{
	private final InternalGraphQLSchemaBuilder builder;

	private TypeDiscovery typeDiscovery;

	private GraphQLBinder()
	{
		builder = new InternalGraphQLSchemaBuilder();
	}

	@NonNull
	public static GraphQLBinder newBinder()
	{
		return new GraphQLBinder();
	}

	@NonNull
	public GraphQLBinder setInstanceFactory(@NonNull InstanceFactory factory)
	{
		Objects.requireNonNull(factory);

		builder.setInstanceFactory(factory);
		return this;
	}

	@NonNull
	public GraphQLBinder setTypeDiscovery(@NonNull TypeDiscovery discovery)
	{
		Objects.requireNonNull(discovery);

		this.typeDiscovery = discovery;
		return this;
	}

	@NonNull
	public GraphQLBinder withType(@NonNull Class<?> type)
	{
		Objects.requireNonNull(type);

		builder.addType(type);
		return this;
	}

	@NonNull
	public GraphQLBinder withResolver(@NonNull GraphQLResolver resolver)
	{
		Objects.requireNonNull(resolver);

		builder.addResolver(resolver);
		return this;
	}

	@NonNull
	public GraphQLBinder withRoot(@NonNull Object instance)
	{
		Objects.requireNonNull(instance);

		builder.addRootType(instance.getClass(), (env) -> instance);
		return this;
	}

	@NonNull
	public GraphQLBinder withDirective(@NonNull GraphQLDirectiveResolver<? extends Annotation> directive)
	{
		Objects.requireNonNull(directive);

		builder.addDirective(directive);
		return this;
	}

	/**
	 * Build a complete schema from the types.
	 *
	 * @return
	 */
	@NonNull
	public GraphQLSchema build()
	{
		if(typeDiscovery != null)
		{
			for(Class<?> c : typeDiscovery.getTypesAnnotatedWith(GraphQLObject.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeDiscovery.getTypesAnnotatedWith(GraphQLInputObject.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeDiscovery.getTypesAnnotatedWith(GraphQLEnum.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeDiscovery.getTypesAnnotatedWith(GraphQLInterface.class))
			{
				builder.addType(c);
			}

			for(Class<?> c : typeDiscovery.getTypesAnnotatedWith(GraphQLUnion.class))
			{
				builder.addType(c);
			}

			for(Object instance : typeDiscovery.getTypesAnnotatedWithAsInstances(GraphQLRoot.class))
			{
				builder.addRootType(instance.getClass(), env -> instance);
			}

			for(Object instance : typeDiscovery.getTypesAnnotatedWithAsInstances(GraphQLAutoRegister.class))
			{
				if(instance instanceof GraphQLResolver)
				{
					builder.addResolver((GraphQLResolver) instance);
				}
			}
		}

		return builder.build();
	}
}
