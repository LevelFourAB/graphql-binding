package se.l4.graphql.binding.internal.datafetchers;

import java.lang.reflect.Field;

import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;

public class FieldDataFetcher<I, T>
	implements DataFetchingSupplier<T>
{
	private final DataFetchingSupplier<Object> contextGetter;
	private final Field field;
	private final DataFetchingConversion<I, T> returnTypeConversion;

	public FieldDataFetcher(
		DataFetchingSupplier<Object> contextGetter,
		Field field,
		DataFetchingConversion<I, T> returnTypeConversion
	)
	{
		this.contextGetter = contextGetter;
		this.field = field;
		this.returnTypeConversion = returnTypeConversion;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(DataFetchingEnvironment environment)
	{
		try
		{
			Object context = contextGetter.get(environment);
			return returnTypeConversion.convert(environment, (I) field.get(context));
		}
		catch(IllegalAccessException | IllegalArgumentException e)
		{
			throw new GraphQLMappingException(e.getMessage(), e);
		}
	}
}
