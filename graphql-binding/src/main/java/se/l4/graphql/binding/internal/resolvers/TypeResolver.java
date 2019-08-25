package se.l4.graphql.binding.internal.resolvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.FieldRef;
import se.l4.commons.types.reflect.MethodRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.DataFetchingSupplier;
import se.l4.graphql.binding.internal.datafetchers.FieldDataFetcher;
import se.l4.graphql.binding.internal.datafetchers.MethodDataFetcher;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.ResolverContext;
import se.l4.graphql.binding.resolver.query.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

public class TypeResolver
	implements GraphQLOutputResolver
{
	private static final DataFetchingSupplier<Object> DEFAULT_FETCHING =
		env -> env.getSource();

	@Override
	public Optional<GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		GraphQLObjectBuilder builder = encounter.newObjectType()
			.over(encounter.getType());

		resolve(encounter, encounter.getType(), DEFAULT_FETCHING, builder);

		return Optional.of(builder.build());
	}

	public static void resolve(
		ResolverContext context,
		TypeRef type,
		DataFetchingSupplier<Object> contextGetter,
		GraphQLObjectBuilder builder
	)
	{
		// Go through all the Java fields in the type and map them
		for(FieldRef field : type.getDeclaredFields())
		{
			if(! field.findAnnotation(GraphQLField.class).isPresent())
			{
				continue;
			}

			if(! field.isPublic())
			{
				throw context.newError(
					Breadcrumb.forMember(field),
					"Field must be public to be useable"
				);
			}

			builder.newField()
				.over(field)
				.setType(context.resolveOutput(field.getType()))
				.withDataFetcher(new FieldDataFetcher<>(contextGetter, field.getField()))
				.done();
		}

		// Go through all the methods in the type and map them
		for(MethodRef method : type.getDeclaredMethods())
		{
			if(! method.findAnnotation(GraphQLField.class).isPresent())
			{
				continue;
			}

			if(! method.isPublic())
			{
				throw context.newError(
					Breadcrumb.forMember(method),
					"Method must be public to be useable"
				);
			}

			GraphQLFieldBuilder<?> fieldBuilder = builder.newField()
				.over(method)
				.setType(context.resolveOutput(method.getReturnType()));

			List<ParameterRef> parameters = method.getParameters();
			List<DataFetchingSupplier<?>> arguments = new ArrayList<>();

			for(ParameterRef parameter : parameters)
			{
				// Resolve the supplier to use for the parameter
				String name = context.getParameterName(parameter);
				arguments.add(env -> env.getArgument(name));

				// Register the argument
				fieldBuilder.newArgument()
					.over(parameter)
					.setType(context.resolveInput(parameter.getType()))
					.done();
			}

			fieldBuilder.withDataFetcher(new MethodDataFetcher<>(
				contextGetter,
				method.getMethod(),
				arguments.toArray(DataFetchingSupplier[]::new)
			))
				.done();
		}
	}
}
