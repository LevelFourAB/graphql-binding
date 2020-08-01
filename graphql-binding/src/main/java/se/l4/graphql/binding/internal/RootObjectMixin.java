package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.list.ListIterable;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.annotations.GraphQLMixinField;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.datafetchers.MethodDataFetcher;
import se.l4.graphql.binding.internal.factory.ArgumentResolver;
import se.l4.graphql.binding.internal.factory.MemberKey;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixin;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixinEncounter;
import se.l4.ylem.types.reflect.MethodRef;
import se.l4.ylem.types.reflect.ParameterRef;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Implementation of {@link GraphQLObjectMixin} that wraps a root object
 * and looks for methods annotated with {@link GraphQLMixinField} and applies
 * them when the type is built.
 */
public class RootObjectMixin
	implements GraphQLObjectMixin
{
	private final TypeRef root;
	private final DataFetchingSupplier<?> supplier;

	public RootObjectMixin(TypeRef root, DataFetchingSupplier<?> supplier)
	{
		this.root = root;
		this.supplier = supplier;
	}

	@Override
	public boolean supportsOutputMixin(TypeRef type)
	{
		return true;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void mixin(GraphQLObjectMixinEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();
		Class<?> erasedType = encounter.getType().getErasedType();

		Set<MemberKey> handled = new HashSet<>();

		root.visitHierarchy(type -> {

			for(MethodRef method : type.getDeclaredMethods())
			{
				if(! method.hasAnnotation(GraphQLMixinField.class))
				{
					continue;
				}

				// Check if this is already handled
				if(! handled.add(MemberKey.create(method))) continue;

				if(! method.isPublic())
				{
					throw context.newError(
						Breadcrumb.forMember(method),
						"Method must be public to be useable"
					);
				}

				ListIterable<ParameterRef> parameters = method.getParameters();
				if(parameters.isEmpty()
					|| ! parameters.get(0).hasAnnotation(GraphQLSource.class)
					|| ! parameters.get(0).getType().getErasedType().isAssignableFrom(erasedType))
				{
					continue;
				}

				ResolvedGraphQLType<? extends GraphQLOutputType> fieldType = context.resolveOutput(method.getReturnType());

				GraphQLFieldBuilder<?> fieldBuilder = encounter.newField()
					.over(method)
					.setType(fieldType.getGraphQLType());

				List<DataFetchingSupplier<?>> arguments = new ArrayList<>();

				for(ParameterRef parameter : parameters)
				{
					if(parameter.hasAnnotation(GraphQLSource.class))
					{
						arguments.add(env -> env.getSource());
					}
					else
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
				}

				fieldBuilder.withSupplier(new MethodDataFetcher(
					supplier,
					method.getMethod(),
					arguments,
					fieldType.getConversion()
				))
					.done();
			}

			return true;
		});
	}
}
