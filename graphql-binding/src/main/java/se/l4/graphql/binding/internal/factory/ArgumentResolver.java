package se.l4.graphql.binding.internal.factory;

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;

/**
 * Resolver that extracts a GraphQL argument for use within a method call.
 */
public class ArgumentResolver
	implements DataFetchingSupplier<Object>
{
	private final String name;
	private final DataFetchingConversion<Object, Object> conversion;
	private final DataFetchingSupplier<Object> defaultValue;

	public ArgumentResolver(
		String name,
		DataFetchingConversion<Object, Object> conversion,
		DataFetchingSupplier<Object> defaultValue
	)
	{
		this.name = name;
		this.conversion = conversion;
		this.defaultValue = defaultValue;
	}

	@Override
	public Object get(DataFetchingEnvironment env)
	{
		Map<String, Object> arguments = env.getArguments();
		if(! arguments.containsKey(name))
		{
			return defaultValue.get(env);
		}

		Object value = env.getArgument(name);
		return conversion.convert(env, value);
	}
}
