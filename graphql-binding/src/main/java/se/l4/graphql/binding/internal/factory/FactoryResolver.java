package se.l4.graphql.binding.internal.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import graphql.schema.DataFetchingEnvironment;
import se.l4.commons.types.reflect.ConstructorRef;
import se.l4.commons.types.reflect.ExecutableRef;
import se.l4.commons.types.reflect.MethodRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;

public class FactoryResolver
{
	private FactoryResolver()
	{
	}

	public static List<Factory<?, ?>> resolveFactories(
		GraphQLResolverContext context,
		TypeRef ref
	)
	{
		List<Factory<?, ?>> result = new ArrayList<>();

		generateConstructorFactories(context, ref, result);
		generateMethodFactories(context, ref, result);

		return result;
	}

	private static void generateConstructorFactories(
		GraphQLResolverContext context,
		TypeRef ref,
		List<Factory<?, ?>> result
	)
	{
		for(ConstructorRef constructor : ref.getConstructors())
		{
			if(! constructor.findAnnotation(GraphQLFactory.class).isPresent())
			{
				// Not marked with @GraphQLFactory, skip this constructor
				continue;
			}

			Optional<TypeRef> sourceType = findCreatableType(constructor);
			if(! sourceType.isPresent())
			{
				throw context.newError(
					Breadcrumb.forMember(constructor),
					"A parameter with @GraphQLSource is required to be able " +
					"to automatically construct type"
				);
			}

			DataFetchingSupplier<?>[] suppliers = getParameterSuppliers(context, constructor);
			result.add(new ConstructorFactory(
				sourceType.get(),
				ref,

				suppliers,
				constructor.getConstructor()
			));
		}
	}

	private static void generateMethodFactories(
		GraphQLResolverContext context,
		TypeRef ref,
		List<Factory<?, ?>> result
	)
	{
		for(MethodRef method : ref.getMethods())
		{
			if(! method.findAnnotation(GraphQLFactory.class).isPresent())
			{
				// Not marked with @GraphQLFactory, skip this constructor
				continue;
			}

			if(! method.isStatic())
			{
				throw context.newError(
					Breadcrumb.forMember(method),
					"Factory methods must be static"
				);
			}

			Optional<TypeRef> sourceType = findCreatableType(method);
			if(! sourceType.isPresent())
			{
				throw context.newError(
					Breadcrumb.forMember(method),
					"A parameter with @GraphQLSource is required to be able " +
					"to automatically construct type"
				);
			}

			DataFetchingSupplier<?>[] suppliers = getParameterSuppliers(context, method);
			result.add(new MethodFactory(
				sourceType.get(),
				method.getReturnType(),

				suppliers,
				method.getMethod()
			));
		}
	}

	private static Optional<TypeRef> findCreatableType(ExecutableRef executable)
	{
		for(ParameterRef parameter : executable.getParameters())
		{
			if(parameter.findAnnotation(GraphQLSource.class).isPresent())
			{
				return Optional.of(parameter.getType());
			}
		}

		return Optional.empty();
	}

	private static DataFetchingSupplier<?>[] getParameterSuppliers(
		GraphQLResolverContext context,
		ExecutableRef executable
	)
	{
		List<ParameterRef> parameters = executable.getParameters();
		DataFetchingSupplier<?>[] suppliers = new DataFetchingSupplier[parameters.size()];
		int i = 0;
		for(ParameterRef parameter : parameters)
		{
			Optional<DataFetchingSupplier<?>> supplier = context.resolveSupplier(
				parameter.getAnnotations(),
				parameter.getType()
			);

			if(supplier.isPresent())
			{
				suppliers[i] = supplier.get();
			}
			else if(parameter.findAnnotation(GraphQLSource.class).isPresent())
			{
				// No supplier, mark as the source
				suppliers[i] = null;
			}
			else
			{
				// Default to letting the instance factory handle it
				Supplier<?> instanceSupplier = context.getInstanceFactory().supplier(
					parameter.getType().getType(),
					parameter.getAnnotations()
				);
				suppliers[i] = env -> instanceSupplier.get();
			}

			i++;
		}

		return suppliers;
	}

	private static abstract class AbstractFactory
		implements Factory<Object, Object>
	{
		private final TypeRef input;
		private final TypeRef output;

		private final DataFetchingSupplier<?>[] parameterSuppliers;

		public AbstractFactory(
			TypeRef input,
			TypeRef output,
			DataFetchingSupplier<?>[] parameterSuppliers
		)
		{
			this.input = input;
			this.output = output;

			this.parameterSuppliers = parameterSuppliers;
		}

		@Override
		public TypeRef getInput()
		{
			return input;
		}

		@Override
		public TypeRef getOutput()
		{
			return output;
		}

		@Override
		public Object convert(DataFetchingEnvironment env, Object source)
		{
			Object[] args = Arrays.stream(parameterSuppliers)
				.map(supplier -> {
					if(supplier == null)
					{
						return source;
					}
					else
					{
						return supplier.get(env);
					}
				})
				.toArray(Object[]::new);

			return create(args);
		}

		public abstract Object create(Object[] args);

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + "{"
				+ "input=" + input + ", output=" + output
				+ "}";
		}
	}

	private static class ConstructorFactory
		extends AbstractFactory
	{
		private final Constructor<?> constructor;

		public ConstructorFactory(
			TypeRef input,
			TypeRef output,
			DataFetchingSupplier<?>[] parameterSuppliers,
			Constructor<?> constructor
		)
		{
			super(input, output, parameterSuppliers);

			this.constructor = constructor;
		}

		@Override
		public Object create(Object[] args)
		{
			try
			{
				return constructor.newInstance(args);
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException e)
			{
				throw new GraphQLMappingException("Unable to create object; " + e.getMessage(), e);
			}
			catch(InvocationTargetException e)
			{
				throw new GraphQLMappingException("Unable to create object; " + e.getCause().getMessage(), e.getCause());
			}
		}
	}

	private static class MethodFactory
		extends AbstractFactory
	{
		private final Method method;

		public MethodFactory(
			TypeRef input,
			TypeRef output,
			DataFetchingSupplier<?>[] parameterSuppliers,
			Method method
		)
		{
			super(input, output, parameterSuppliers);

			this.method = method;
		}

		@Override
		public Object create(Object[] args)
		{
			try
			{
				return (Object) method.invoke(null, args);
			}
			catch(IllegalAccessException | IllegalArgumentException e)
			{
				throw new GraphQLMappingException("Unable to create object; " + e.getMessage(), e);
			}
			catch(InvocationTargetException e)
			{
				throw new GraphQLMappingException("Unable to create object; " + e.getCause().getMessage(), e.getCause());
			}
		}
	}
}
