package se.l4.graphql.binding.internal.resolvers;

import java.util.Collection;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Resolver for a list.
 */
public class ListResolver
	implements GraphQLOutputResolver, GraphQLInputResolver
{
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();

		ResolvedGraphQLType<? extends GraphQLOutputType> componentType = encounter.getType().getTypeParameter(0)
			.map(context::resolveOutput)
			.orElseThrow(() -> context.newError(
				"Could not resolve a GraphQL type for `" + encounter.getType().toTypeName() + "`"
			));

		return ResolvedGraphQLType.forType(
			GraphQLList.list(componentType.getGraphQLType())
		).withConversion(new ListConverter(componentType.getConversion()));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();

		ResolvedGraphQLType<? extends GraphQLInputType> componentType = encounter.getType().getTypeParameter(0)
			.map(context::resolveInput)
			.orElseThrow(() -> context.newError(
				"Could not resolve a GraphQL type for `" + encounter.getType().toTypeName() + "`"
			));

		return ResolvedGraphQLType.forType(
			GraphQLList.list(componentType.getGraphQLType())
		).withConversion(new ListConverter(componentType.getConversion()));
	}

	private static class ListConverter<I, O>
		implements DataFetchingConversion<Collection<I>, Collection<O>>
	{
		private final DataFetchingConversion<I, O> conversion;

		public ListConverter(DataFetchingConversion<I, O> conversion)
		{
			this.conversion = conversion;
		}

		@Override
		public Collection<O> convert(DataFetchingEnvironment env, Collection<I> object)
		{
			return object.stream()
				.map(item -> conversion.convert(env, item))
				.collect(Collectors.toList());
		}
	}
}
