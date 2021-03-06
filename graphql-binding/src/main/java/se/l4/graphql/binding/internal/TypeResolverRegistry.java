package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.List;

import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Registry for keeping track of and finding resolvers for both input and
 * output types.
 */
public class TypeResolverRegistry
{
	private final List<GraphQLOutputResolver> outputResolvers;
	private final List<GraphQLInputResolver> inputResolvers;

	public TypeResolverRegistry()
	{
		this.outputResolvers = new ArrayList<>();
		this.inputResolvers = new ArrayList<>();
	}

	public void add(GraphQLResolver resolver)
	{
		if(resolver instanceof GraphQLOutputResolver)
		{
			outputResolvers.add((GraphQLOutputResolver) resolver);
		}

		if(resolver instanceof GraphQLInputResolver)
		{
			inputResolvers.add((GraphQLInputResolver) resolver);
		}
	}

	/**
	 * Get an output resolver for the given type. Output resolvers are
	 * registered using a lower bound, so an output resolver bound to
	 *
	 * @param type
	 *   the {@link Class} to find a resolver for
	 * @return
	 *   the found resolver
	 */
	public List<GraphQLOutputResolver> getOutputResolver(TypeRef type)
	{
		List<GraphQLOutputResolver> resolvers = new ArrayList<>();

		for(GraphQLOutputResolver resolver : outputResolvers)
		{
			if(resolver.supportsOutput(type))
			{
				resolvers.add(resolver);
			}
		}

		return resolvers;
	}

	/**
	 * Get an output resolver for the given type.
	 *
	 * @param type
	 *   the {@link Class} to find a resolver for
	 * @return
	 *   the found resolver
	 */
	public List<GraphQLInputResolver> getInputResolver(TypeRef type)
	{
		List<GraphQLInputResolver> resolvers = new ArrayList<>();

		for(GraphQLInputResolver resolver : inputResolvers)
		{
			if(resolver.supportsInput(type))
			{
				resolvers.add(resolver);
			}
		}

		return resolvers;
	}

}
