package se.l4.graphql.binding.internal.datafetchers;

import java.lang.reflect.Field;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.internal.DataFetchingSupplier;

public class FieldDataFetcher<T>
	implements DataFetcher<T>
{
	private final DataFetchingSupplier<Object> contextGetter;
	private final Field field;

	public FieldDataFetcher(
		DataFetchingSupplier<Object> contextGetter,
		Field field
	)
	{
		this.contextGetter = contextGetter;
		this.field = field;
	}

	@Override
	public T get(DataFetchingEnvironment environment)
		throws Exception
	{
		Object context = contextGetter.get(environment);
		return (T) field.get(context);
	}
}
