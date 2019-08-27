package se.l4.graphql.binding.internal.datafetchers;

import java.lang.reflect.Field;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import se.l4.commons.types.conversion.ConversionFunction;
import se.l4.graphql.binding.internal.DataFetchingSupplier;

public class FieldDataFetcher<I, T>
	implements DataFetcher<T>
{
	private final DataFetchingSupplier<Object> contextGetter;
	private final Field field;
	private final ConversionFunction<I, T> returnTypeConversion;

	public FieldDataFetcher(
		DataFetchingSupplier<Object> contextGetter,
		Field field,
		ConversionFunction<I, T> returnTypeConversion
	)
	{
		this.contextGetter = contextGetter;
		this.field = field;
		this.returnTypeConversion = returnTypeConversion;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(DataFetchingEnvironment environment)
		throws Exception
	{
		Object context = contextGetter.get(environment);
		return returnTypeConversion.convert((I) field.get(context));
	}
}
