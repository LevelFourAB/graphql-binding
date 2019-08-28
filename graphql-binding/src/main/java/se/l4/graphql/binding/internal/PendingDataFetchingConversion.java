package se.l4.graphql.binding.internal;

import graphql.schema.DataFetchingEnvironment;

public class PendingDataFetchingConversion<I, O>
	implements DataFetchingConversion<I, O>
{
	private volatile DataFetchingConversion<I, O> actual;

	@Override
	public O convert(DataFetchingEnvironment environment, I object)
	{
		return actual.convert(environment, object);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void update(DataFetchingConversion<?, ?> conversion)
	{
		this.actual = (DataFetchingConversion) conversion;
	}
}
