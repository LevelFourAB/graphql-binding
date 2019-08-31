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

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
				sourceType.get().getErasedType(),
				ref.getErasedType(),

				suppliers,
				constructor.getConstructor()
			));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
				sourceType.get().getErasedType(),
				method.getReturnType().getErasedType(),

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
			Optional<DataFetchingSupplier<?>> supplier = resolveEnvironmentSupplier(context, parameter);
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

	public static Optional<DataFetchingSupplier<?>> resolveEnvironmentSupplier(
		GraphQLResolverContext context,
		ParameterRef ref
	)
	{
		return Optional.empty();
	}

	private static abstract class AbstractFactory<I, O>
		implements Factory<I, O>
	{
		private final Class<I> input;
		private final Class<O> output;

		private final DataFetchingSupplier<?>[] parameterSuppliers;

		public AbstractFactory(
			Class<I> input,
			Class<O> output,
			DataFetchingSupplier<?>[] parameterSuppliers
		)
		{
			this.input = input;
			this.output = output;

			this.parameterSuppliers = parameterSuppliers;
		}

		@Override
		public Class<I> getInput()
		{
			return input;
		}

		@Override
		public Class<O> getOutput()
		{
			return output;
		}

		@Override
		public O convert(DataFetchingEnvironment env, I source)
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

		public abstract O create(Object[] args);

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + "{"
				+ "input=" + input + ", output=" + output
				+ "}";
		}
	}

	private static class ConstructorFactory<I, O>
		extends AbstractFactory<I, O>
	{
		private final Constructor<O> constructor;

		public ConstructorFactory(
			Class<I> input,
			Class<O> output,
			DataFetchingSupplier<?>[] parameterSuppliers,
			Constructor<O> constructor
		)
		{
			super(input, output, parameterSuppliers);

			this.constructor = constructor;
		}

		@Override
		public O create(Object[] args)
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

	private static class MethodFactory<I, O>
		extends AbstractFactory<I, O>
	{
		private final Method method;

		public MethodFactory(
			Class<I> input,
			Class<O> output,
			DataFetchingSupplier<?>[] parameterSuppliers,
			Method method
		)
		{
			super(input, output, parameterSuppliers);

			this.method = method;
		}

		@Override
		@SuppressWarnings("unchecked")
		public O create(Object[] args)
		{
			try
			{
				return (O) method.invoke(null, args);
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
