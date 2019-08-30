package se.l4.graphql.binding.internal.resolvers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.FieldRef;
import se.l4.commons.types.reflect.MethodRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
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

		resolve(encounter.getContext(), type, DEFAULT_FETCHING, builder, GraphQLField.class);

		return ResolvedGraphQLType.forType(builder.build());
	}

	public static void resolve(
		GraphQLResolverContext context,
		TypeRef initialType,
		DataFetchingSupplier<Object> contextGetter,
		GraphQLObjectBuilder builder,
		Class<? extends Annotation> annotation
	)
	{
		Set<MemberKey> handled = new HashSet<>();

		initialType.visitHierarchy(type -> {
			// Resolve if this type implements a certain interface
			if(type.hasAnnotation(GraphQLInterface.class))
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

					ResolvedGraphQLType<? extends GraphQLOutputType> fieldType = context.resolveOutput(field.getType());

					builder.newField()
						.over(field)
						.setType(fieldType.getGraphQLType())
						.withDataFetcher(new FieldDataFetcher<>(
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

					ResolvedGraphQLType<? extends GraphQLOutputType> fieldType = context.resolveOutput(method.getReturnType());

					GraphQLFieldBuilder<?> fieldBuilder = builder.newField()
						.over(method)
						.setType(fieldType.getGraphQLType());

					List<ParameterRef> parameters = method.getParameters();
					List<DataFetchingSupplier<?>> arguments = new ArrayList<>();

					for(ParameterRef parameter : parameters)
					{
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

					fieldBuilder.withDataFetcher(new MethodDataFetcher<>(
						contextGetter,
						method.getMethod(),
						arguments.toArray(DataFetchingSupplier[]::new),
						fieldType.getConversion()
					))
						.done();
				});
			}

			return true;
		});
	}

}
