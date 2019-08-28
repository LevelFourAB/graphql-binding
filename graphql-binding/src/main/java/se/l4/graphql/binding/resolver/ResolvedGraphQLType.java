package se.l4.graphql.binding.resolver;

import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import se.l4.graphql.binding.internal.DataFetchingConversion;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Output handler as resolved by {@link GraphQLOutputResolver}.
 */
public class ResolvedGraphQLType<T extends GraphQLType>
{
	private static final ResolvedGraphQLType<?> NONE = new ResolvedGraphQLType<>(null, null);
	private static final DataFetchingConversion<?, ?> IDENTITY = (env, i) -> i;

	private final T type;
	private final DataFetchingConversion<?, ?> conversion;

	private ResolvedGraphQLType(
		T type,
		DataFetchingConversion<?, ?> conversion
	)
	{
		this.type = type;
		this.conversion = conversion;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends GraphQLOutputType> ResolvedGraphQLType<T> none()
	{
		return (ResolvedGraphQLType) NONE;
	}

	public static <T extends GraphQLOutputType> ResolvedGraphQLType<T> forType(T type)
	{
		return new ResolvedGraphQLType<>(type, IDENTITY);
	}

	/**
	 * Get if this output is present.
	 *
	 * @return
	 */
	public boolean isPresent()
	{
		return type != null;
	}

	/**
	 * Get the defined GraphQL type.
	 *
	 * @return
	 */
	public T getGraphQLType()
	{
		if(! isPresent())
		{
			throw new IllegalStateException();
		}

		return type;
	}

	public ResolvedGraphQLType<GraphQLNonNull> nonNull()
	{
		if(! isPresent())
		{
			return none();
		}

		return new ResolvedGraphQLType<>(
			GraphQLNonNull.nonNull(type),
			conversion
		);
	}

	/**
	 * Conversion that can convert from an object returned by the parent field
	 * into an object that the fetchers defined by {@link #getGraphQLType()}
	 * can act upon.
	 *
	 * @return
	 */
	public DataFetchingConversion<?, ?> getConversion()
	{
		if(! isPresent())
		{
			throw new IllegalStateException();
		}

		return conversion;
	}

	public <I> ResolvedGraphQLType<T> withConversion(DataFetchingConversion<I, T> conversion)
	{
		return new ResolvedGraphQLType<>(type, conversion);
	}
}
