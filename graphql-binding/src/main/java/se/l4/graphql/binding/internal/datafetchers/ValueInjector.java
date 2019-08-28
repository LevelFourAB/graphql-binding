package se.l4.graphql.binding.internal.datafetchers;

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;

public interface ValueInjector
{
	void inject(
		DataFetchingEnvironment environment,
		Object instance,
		Map<String, Object> data
	);
}
