package se.l4.graphql.binding.internal.resolvers;

import java.util.Collection;
import java.util.stream.Collectors;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.conversion.ConversionFunction;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Resolver for a list.
 */
public class ListResolver
	implements GraphQLOutputResolver
{
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		ResolvedGraphQLType<? extends GraphQLOutputType> componentType = encounter.getType().getTypeParameter(0)
			.map(encounter::resolveOutput)
			.orElseThrow(() -> encounter.newError(
				"Could not resolve a GraphQL type for `" + encounter.getType().toTypeName() + "`"
			));

		return ResolvedGraphQLType.forType(
			GraphQLList.list(componentType.getGraphQLType())
		).withConversion(new ListConverter(componentType.getConversion()));
	}

	private static class ListConverter<I, O>
		implements ConversionFunction<Collection<I>, Collection<O>>
	{
		private final ConversionFunction<I, O> conversion;

		public ListConverter(ConversionFunction<I, O> conversion)
		{
			this.conversion = conversion;
		}

		@Override
		public Collection<O> convert(Collection<I> object)
		{
			return object.stream()
				.map(conversion::convert)
				.collect(Collectors.toList());
		}
	}
}
