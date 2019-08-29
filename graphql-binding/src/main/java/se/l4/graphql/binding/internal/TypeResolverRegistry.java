package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.l4.commons.types.matching.ClassMatchingHashMultimap;
import se.l4.commons.types.matching.ClassMatchingMultimap;
import se.l4.commons.types.matching.MatchedType;
import se.l4.graphql.binding.internal.resolvers.MultiInputResolver;
import se.l4.graphql.binding.internal.resolvers.MultiOutputResolver;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Registry for keeping track of and finding {@link GraphQLOutputResolver}s.
 */
public class TypeResolverRegistry
{
	private final ClassMatchingMultimap<Object, GraphQLOutputResolver> outputTypes;
	private final ClassMatchingMultimap<Object, GraphQLInputResolver> inputTypes;

	public TypeResolverRegistry()
	{
		this.outputTypes = new ClassMatchingHashMultimap<>();
		this.inputTypes = new ClassMatchingHashMultimap<>();
	}

	public void bindAny(Class<?> type, GraphQLResolver resolver)
	{
		if(resolver instanceof GraphQLOutputResolver)
		{
			bindOutput(type, (GraphQLOutputResolver) resolver);
		}

		if(resolver instanceof GraphQLInputResolver)
		{
			bindInput(type, (GraphQLInputResolver) resolver);
		}
	}

	/**
	 * Bind an output resolver for the given type.
	 *
	 * @param type
	 * @param resolver
	 */
	public void bindOutput(Class<?> type, GraphQLOutputResolver resolver)
	{
		outputTypes.put(type, resolver);
	}

	/**
	 * Bind an input resolver for the given type.
	 *
	 * @param type
	 * @param resolver
	 */
	public void bindInput(Class<?> type, GraphQLInputResolver resolver)
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
		List<MatchedType<Object, GraphQLOutputResolver>> matching = outputTypes.getAll((Class) type);
		for(MatchedType<Object, GraphQLOutputResolver> out : matching)
		{
			resolvers.add(out.getData());
		}

		if(type.isInterface())
		{
			matching = outputTypes.getAll(Object.class);
			for(MatchedType<Object, GraphQLOutputResolver> out : matching)
			{
				resolvers.add(out.getData());
			}
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
		List<MatchedType<Object, GraphQLInputResolver>> matching = inputTypes.getAll((Class) type);
		for(MatchedType<Object, GraphQLInputResolver> in : matching)
		{
			resolvers.add(in.getData());
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
			return Optional.of(new MultiInputResolver(resolvers));
		}
	}

}
