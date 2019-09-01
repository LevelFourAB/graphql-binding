package se.l4.graphql.binding.internal.datafetchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;

public class MethodDataFetcher<I, T>
	implements DataFetchingSupplier<T>
{
	private final DataFetchingSupplier<Object> contextGetter;
	private final Method method;
	private final DataFetchingSupplier<?>[] parameters;
	private final DataFetchingConversion<I, T> returnTypeConversion;

	public MethodDataFetcher(
		DataFetchingSupplier<Object> contextGetter,
		Method method,
		Collection<DataFetchingSupplier<?>> parameters,
		DataFetchingConversion<I, T> returnTypeConversion
	)
	{
		this.contextGetter = contextGetter;
		this.method = method;
		this.parameters = parameters.toArray(new DataFetchingSupplier[parameters.size()]);
		this.returnTypeConversion = returnTypeConversion;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(DataFetchingEnvironment environment)
	{
		Object context = contextGetter.get(environment);

		try
		{
			I result = (I) method.invoke(
				context,
				Arrays.stream(parameters)
					.map(p -> p.get(environment))
					.toArray()
			);

			return returnTypeConversion.convert(environment, result);
		}
		catch(InvocationTargetException e)
		{
			throw new GraphQLMappingException(e.getCause().getMessage(), e.getCause());
		}
		catch(IllegalAccessException | IllegalArgumentException e)
		{
			throw new GraphQLMappingException(e.getMessage(), e);
		}

	}
}
