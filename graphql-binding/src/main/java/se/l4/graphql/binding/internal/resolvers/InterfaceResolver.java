package se.l4.graphql.binding.internal.resolvers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.MethodRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.internal.factory.FactoryResolver;
import se.l4.graphql.binding.internal.factory.MemberKey;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLInterfaceBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;

public class InterfaceResolver
	implements GraphQLOutputResolver
{
	@Override
	public boolean supportsOutput(TypeRef type)
	{
		return type.hasAnnotation(GraphQLInterface.class);
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(
		GraphQLOutputEncounter encounter
	)
	{
		TypeRef initialType = encounter.getType();

		GraphQLResolverContext context = encounter.getContext();

		GraphQLInterfaceBuilder builder = encounter.newInterfaceType()
			.over(initialType);

		Set<MemberKey> handled = new HashSet<>();

		initialType.visitHierarchy(type -> {
			// Go through all the methods in the type and map them
			for(MethodRef method : type.getDeclaredMethods())
			{
				// Check if this is already handled
				if(! handled.add(MemberKey.create(method))) continue;

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

				ResolvedGraphQLType<? extends GraphQLOutputType> fieldType = context.resolveOutput(method.getReturnType());

				GraphQLFieldBuilder<?> fieldBuilder = builder.newField()
					.over(method)
					.setType(fieldType.getGraphQLType());

				List<ParameterRef> parameters = method.getParameters();

				for(ParameterRef parameter : parameters)
				{
					Optional<DataFetchingSupplier<?>> supplier = FactoryResolver.resolveEnvironmentSupplier(
						context,
						parameter,
						parameter.getType()
					);

					// If an environment supplier can be resolved this isn't an input argument
					if(supplier.isPresent()) continue;

					ResolvedGraphQLType<? extends GraphQLInputType> argumentType = context.resolveInput(parameter.getType());

					// Register the argument
					fieldBuilder.newArgument()
						.over(parameter)
						.setType(argumentType.getGraphQLType())
						.done();
				}

				fieldBuilder.done();
			}

			return true;
		});

		for(TypeRef type : context.findExtendingTypes(initialType))
		{
			builder.addImplementation(type);
		}

		return ResolvedGraphQLType.forType(builder.build());
	}

	@Override
	public String toString()
	{
		return "@" + GraphQLInterface.class.getSimpleName();
	}

}
