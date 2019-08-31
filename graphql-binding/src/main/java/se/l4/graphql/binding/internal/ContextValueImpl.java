package se.l4.graphql.binding.internal;

import java.util.Objects;
import java.util.Optional;

import graphql.GraphQLContext;
import se.l4.graphql.binding.ContextValue;

/**
 * Implementation of {@link ContextValue}.
 */
public class ContextValueImpl<T>
	implements ContextValue<T>
{
	private final GraphQLContext context;
	private final String name;

	private Optional<T> data;

	public ContextValueImpl(
		GraphQLContext context,
		String name
	)
	{
		this.context = context;
		this.name = name;

		this.data = context.getOrEmpty(name);
	}

	@Override
	public boolean isPresent()
	{
		return data.isPresent();
	}

	@Override
	public T get()
	{
		return data.get();
	}

	@Override
	public Optional<T> asOptional()
	{
		return data;
	}

	@Override
	public void update(T value)
	{
		Objects.requireNonNull(value);

		context.put(name, value);
		this.data = Optional.of(value);
	}

	@Override
	public void clear()
	{
		context.delete(name);
		this.data = Optional.empty();
	}
}
