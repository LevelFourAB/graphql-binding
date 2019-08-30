package se.l4.graphql.binding.resolver;

import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;

/**
 * Output handler as resolved by a {@link GraphQLOutputResolver} or a
 * {@link GraphQLInputResolver}.
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

	/**
	 * Get an instance that represents that no type was resolved.
	 *
	 * @return
	 *   instance that always returns false from {@link #isPresent()}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends GraphQLType> ResolvedGraphQLType<T> none()
	{
		return (ResolvedGraphQLType) NONE;
	}

	/**
	 * Create a resolved result over the given type.
	 *
	 * @param type
	 *   the GraphQL type
	 * @return
	 *   instance resolved to the specified type with no conversion
	 */
	public static <T extends GraphQLType> ResolvedGraphQLType<T> forType(T type)
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

	/**
	 * Get a version of this type that represents a non-null value.
	 *
	 * @return
	 */
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

	/**
	 * Get a version of this type using the specified conversion to modify the
	 * Java type.
	 *
	 * @param conversion
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<T> withConversion(DataFetchingConversion<?, ?> conversion)
	{
		return new ResolvedGraphQLType<>(
			type,
			this.conversion == IDENTITY ? conversion : this.conversion.and((DataFetchingConversion) conversion)
		);
	}
}
