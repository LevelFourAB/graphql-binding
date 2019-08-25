package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.l4.commons.types.matching.ClassMatchingHashMultimap;
import se.l4.commons.types.matching.ClassMatchingMultimap;
import se.l4.graphql.binding.annotations.GraphQLType;
import se.l4.graphql.binding.internal.resolvers.ArrayResolver;
import se.l4.graphql.binding.internal.resolvers.MultiInputResolver;
import se.l4.graphql.binding.internal.resolvers.MultiOutputResolver;
import se.l4.graphql.binding.internal.resolvers.TypeResolver;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Registry for keeping track of and finding {@link GraphQLOutputResolver}s.
 */
public class TypeResolverRegistry
{
	private static final GraphQLOutputResolver ARRAY_RESOLVER = new ArrayResolver();
	private static final GraphQLOutputResolver TYPE_RESOLVER = new TypeResolver();

	private final ClassMatchingMultimap<Object, GraphQLOutputResolver> outputTypes;
	private final ClassMatchingMultimap<Object, GraphQLInputResolver> inputTypes;

	public TypeResolverRegistry()
	{
		this.outputTypes = new ClassMatchingHashMultimap<>();
		this.inputTypes = new ClassMatchingHashMultimap<>();
	}

	/**
	 * Bind an output resolver for the given type.
	 *
	 * @param type
	 * @param resolver
	 */
	public <T> void bindOutput(Class<T> type, GraphQLOutputResolver resolver)
	{
		outputTypes.put(type, resolver);
	}

	/**
	 * Bind an input resolver for the given type.
	 *
	 * @param type
	 * @param resolver
	 */
	public <T> void bindInput(Class<T> type, GraphQLInputResolver resolver)
	{
		inputTypes.put(type, resolver);
	}

	/**
	 * Get an output resolver for the given type.
	 *
	 * @param type
	 *   the {@link Class} to find a resolver for
	 * @return
	 *   the found resolver
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Optional<GraphQLOutputResolver> getOutputResolver(Class<?> type)
	{
		List<GraphQLOutputResolver> resolvers = new ArrayList<>();

		// Resolve ones that use types
		resolvers.addAll(outputTypes.getAll((Class) type));

		if(type.isArray())
		{
			// Arrays have special treatment, always use the array resolver
			resolvers.add(ARRAY_RESOLVER);
		}
		else if(type.isAnnotationPresent(GraphQLType.class))
		{
			resolvers.add(TYPE_RESOLVER);
		}

		if(resolvers.isEmpty())
		{
			return Optional.empty();
		}
		else if(resolvers.size() == 1)
		{
			return Optional.of(resolvers.get(0));
		}
		else
		{
			return Optional.of(new MultiOutputResolver(resolvers));
		}
	}

	/**
	 * Get an output resolver for the given type.
	 *
	 * @param type
	 *   the {@link Class} to find a resolver for
	 * @return
	 *   the found resolver
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Optional<GraphQLInputResolver> getInputResolver(Class<?> type)
	{
		List<GraphQLInputResolver> resolvers = new ArrayList<>();

		// Resolve ones that use types
		resolvers.addAll(outputTypes.getAll((Class) type));

		if(resolvers.isEmpty())
		{
			return Optional.empty();
		}
		else if(resolvers.size() == 1)
		{
			return Optional.of(resolvers.get(0));
		}
		else
		{
			return Optional.of(new MultiInputResolver(resolvers));
		}
	}

}
