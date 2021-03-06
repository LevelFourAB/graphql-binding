package se.l4.graphql.binding.internal.resolvers;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLDelegatingResolver;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.ylem.types.reflect.TypeRef;

public class ArrayResolver
	implements GraphQLOutputResolver, GraphQLDelegatingResolver
{

	@Override
	public boolean supportsOutput(TypeRef type)
	{
		return type.isArray();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		TypeRef type = encounter.getType();

		GraphQLResolverContext context = encounter.getContext();

		ResolvedGraphQLType<? extends GraphQLOutputType> componentType = type.getComponentType()
			.map(context::maybeResolveOutput)
			.get();

		if(! componentType.isPresent())
		{
			return ResolvedGraphQLType.none();
		}

		return ResolvedGraphQLType.forType(
			GraphQLList.list(componentType.getGraphQLType())
		).withOutputConversion(new ArrayConverter(componentType.getConversion()));
	}

	@Override
	public String toString()
	{
		return "Array";
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
			if(object == null) return null;

			return Arrays.stream(object)
				.map(item -> conversion.convert(env, item))
				.collect(Collectors.toList());
		}
	}
}
