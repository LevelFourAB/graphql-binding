package se.l4.graphql.binding.internal.datafetchers;

import java.lang.reflect.Field;
import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.resolver.DataFetchingConversion;

public class FieldInjector
	implements ValueInjector
{
	private final Field field;
	private final String name;
	private final DataFetchingConversion<Object, Object> conversion;

	public FieldInjector(
		Field field,
		String name,
		DataFetchingConversion<?, ?> conversion
	)
	{
		this.field = field;
		this.name = name;
		this.conversion = (DataFetchingConversion) conversion;
	}

	@Override
	public void inject(
		DataFetchingEnvironment env,
		Object instance,
		Map<String, Object> data
	)
	{
		if(! data.containsKey(name))
		{
			// Data does not contain the name, skip it
			return;
		}

		Object value = conversion.convert(env, data.get(name));
		try
		{
			field.set(instance, value);
		}
		catch(IllegalArgumentException e)
		{
			throw new GraphQLMappingException("Unable to set field; " + e.getMessage(), e);
		}
		catch(IllegalAccessException e)
		{
			throw new GraphQLMappingException("Unable to set field; " + e.getMessage(), e);
		}
	}
}
