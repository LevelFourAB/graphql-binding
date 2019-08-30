package se.l4.graphql.binding.internal.factory;

import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.internal.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.DataFetchingConversion;

/**
 * Resolver that extracts a GraphQL argument for use within a method call.
 */
public class ArgumentResolver
	implements DataFetchingSupplier<Object>
{
	private final String name;
	private final DataFetchingConversion<Object, Object> conversion;

	public ArgumentResolver(String name, DataFetchingConversion<Object, Object> conversion)
	{
		this.name = name;
		this.conversion = conversion;
	}

	@Override
	public Object get(DataFetchingEnvironment env)
	{
		Object value = env.getArgument(name);
		if(value == null)
		{
			return null;
		}

		return conversion.convert(env, value);
	}
}
