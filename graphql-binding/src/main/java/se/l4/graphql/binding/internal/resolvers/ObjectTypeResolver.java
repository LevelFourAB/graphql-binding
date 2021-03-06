package se.l4.graphql.binding.internal.resolvers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.collections.api.list.ListIterable;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.internal.datafetchers.FieldDataFetcher;
import se.l4.graphql.binding.internal.datafetchers.MethodDataFetcher;
import se.l4.graphql.binding.internal.factory.ArgumentResolver;
import se.l4.graphql.binding.internal.factory.MemberKey;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.ylem.types.reflect.FieldRef;
import se.l4.ylem.types.reflect.MethodRef;
import se.l4.ylem.types.reflect.ParameterRef;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Resolver for types annotated with {@link GraphQLObject} that resolve to
 * {@link GraphQLObjectType}.
 */
public class ObjectTypeResolver
	implements GraphQLOutputResolver
{
	private static final DataFetchingSupplier<Object> DEFAULT_FETCHING =
		env -> env.getSource();

	@Override
	public boolean supportsOutput(TypeRef type)
	{
		return type.hasAnnotation(GraphQLObject.class);
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		TypeRef type = encounter.getType();
		if(! type.isFullyResolved())
		{
			// Types must be fully resolved to be usable
			return ResolvedGraphQLType.none();
		}

		GraphQLObjectBuilder builder = encounter.newObjectType()
			.over(encounter.getType());

		GraphQLResolverContext context = encounter.getContext();

		resolve(
			encounter.getContext(),
			type,
			DEFAULT_FETCHING,
			builder,
			GraphQLField.class,
			true,
			context::resolveOutput
		);

		return ResolvedGraphQLType.forType(builder.build());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void resolve(
		GraphQLResolverContext context,
		TypeRef initialType,
		DataFetchingSupplier<Object> contextGetter,
		GraphQLObjectBuilder builder,
		Class<? extends Annotation> annotation,
		boolean allowInterfaces,
		Function<TypeRef, ResolvedGraphQLType<? extends GraphQLOutputType>> resolveOutput
	)
	{
		Set<MemberKey> handled = new HashSet<>();

		initialType.visitHierarchy(type -> {
			// Resolve if this type implements a certain interface
			if(allowInterfaces && type.hasAnnotation(GraphQLInterface.class))
			{
				ResolvedGraphQLType<? extends GraphQLOutputType> resolved = context.resolveOutput(type);
				builder.implement((GraphQLInterfaceType) resolved.getGraphQLType());
			}

			// Go through all the Java fields in the type and map them
			for(FieldRef field : type.getDeclaredFields())
			{
				if(! field.findAnnotation(annotation).isPresent())
				{
					continue;
				}

				// Check if this is already handled
				if(! handled.add(MemberKey.create(field))) continue;

				context.breadcrumb(Breadcrumb.forMember(field), () -> {
					if(! field.isPublic())
					{
						throw context.newError(
							"Field must be public to be useable"
						);
					}

					ResolvedGraphQLType<? extends GraphQLOutputType> fieldType = resolveOutput.apply(field.getType());

					builder.newField()
						.over(field)
						.setType(fieldType.getGraphQLType())
						.withSupplier(new FieldDataFetcher<>(
							contextGetter,
							field.getField(),
							fieldType.getConversion()
						))
						.done();
				});
			}

			// Go through all the methods in the type and map them
			for(MethodRef method : type.getDeclaredMethods())
			{
				if(! method.hasAnnotation(annotation))
				{
					continue;
				}

				// Check if this is already handled
				if(! handled.add(MemberKey.create(method))) continue;

				context.breadcrumb(Breadcrumb.forMember(method), () -> {
					if(! method.isPublic())
					{
						throw context.newError(
							"Method must be public to be useable"
						);
					}

					ResolvedGraphQLType<? extends GraphQLOutputType> fieldType = resolveOutput.apply(method.getReturnType());

					GraphQLFieldBuilder<?> fieldBuilder = builder.newField()
						.over(method)
						.setType(fieldType.getGraphQLType());

					ListIterable<ParameterRef> parameters = method.getParameters();
					List<DataFetchingSupplier<?>> arguments = new ArrayList<>();

					for(ParameterRef parameter : parameters)
					{
						Optional<DataFetchingSupplier<?>> supplier =
							context.resolveSupplier(
								parameter.getAnnotations(),
								parameter.getType()
							);

						if(supplier.isPresent())
						{
							// If the environment supplies this argument, add it and continue
							arguments.add(supplier.get());
							continue;
						}

						// Default case, resolve argument input
						ResolvedGraphQLType<? extends GraphQLInputType> argumentType = context.resolveInput(parameter.getType());

						// Resolve the supplier to use for the parameter
						String name = context.getParameterName(parameter);
						arguments.add(new ArgumentResolver(
							name,
							(DataFetchingConversion) argumentType.getConversion(),
							(DataFetchingSupplier) argumentType.getDefaultValue()
						));

						// Register the argument
						fieldBuilder.newArgument()
							.over(parameter)
							.setType(argumentType.getGraphQLType())
							.done();
					}

					fieldBuilder.withSupplier(new MethodDataFetcher<>(
						contextGetter,
						method.getMethod(),
						arguments,
						fieldType.getConversion()
					))
						.done();
				});
			}

			return true;
		});
	}

	@Override
	public String toString()
	{
		return "@" + GraphQLObject.class.getSimpleName();
	}
}
