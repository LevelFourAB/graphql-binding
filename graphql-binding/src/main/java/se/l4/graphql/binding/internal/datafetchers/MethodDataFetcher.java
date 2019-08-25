package se.l4.graphql.binding.internal.datafetchers;

import java.lang.reflect.Method;
import java.util.Arrays;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.internal.DataFetchingSupplier;

public class MethodDataFetcher<T>
	implements DataFetcher<T>
{
	private final DataFetchingSupplier<Object> contextGetter;
	private final Method method;
	private final DataFetchingSupplier<?>[] parameters;

	public MethodDataFetcher(
		DataFetchingSupplier<Object> contextGetter,
		Method method,
		DataFetchingSupplier<?>[] parameters
	)
	{
		this.contextGetter = contextGetter;
		this.method = method;
		this.parameters = parameters;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(DataFetchingEnvironment environment)
		throws Exception
	{
		Object context = contextGetter.get(environment);
		return (T) method.invoke(
			context,
			Arrays.stream(parameters)
				.map(p -> p.get(environment))
				.toArray()
		);
	}
}
