package se.l4.graphql.binding.internal.resolvers;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

public class ArrayResolver
	implements GraphQLOutputResolver
{

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();

		ResolvedGraphQLType<? extends GraphQLOutputType> componentType = encounter.getType().getComponentType()
			.map(context::resolveOutput)
			.orElseThrow(() -> context.newError(
				"Could not resolve a GraphQL type for `" + encounter.getType().toTypeName() + "`"
			));

		return ResolvedGraphQLType.forType(
			GraphQLList.list(componentType.getGraphQLType())
		).withConversion(new ArrayConverter(componentType.getConversion()));
	}

	private static class ArrayConverter<I, O>
		implements DataFetchingConversion<I[], Collection<O>>
	{
		private final DataFetchingConversion<I, O> conversion;

		public ArrayConverter(DataFetchingConversion<I, O> conversion)
		{
			this.conversion = conversion;
		}

		@Override
		public Collection<O> convert(DataFetchingEnvironment env, I[] object)
		{
			return Arrays.stream(object)
				.map(item -> conversion.convert(env, item))
				.collect(Collectors.toList());
		}
	}
}
