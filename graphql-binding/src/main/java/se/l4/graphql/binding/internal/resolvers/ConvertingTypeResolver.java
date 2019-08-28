package se.l4.graphql.binding.internal.resolvers;

import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.Types;
import se.l4.graphql.binding.internal.DataFetchingConversion;
import se.l4.graphql.binding.internal.factory.Factory;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

public class ConvertingTypeResolver<I, O>
	implements GraphQLOutputResolver
{
	private final Factory<I, O> factory;

	public ConvertingTypeResolver(
		Factory<I, O> factory
	)
	{
		this.factory = factory;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		ResolvedGraphQLType<? extends GraphQLOutputType> type = encounter.resolveOutput(
			Types.reference(factory.getOutput())
		);

		// Get the type but apply a conversion to our type
		return type.withConversion((DataFetchingConversion) factory);
	}

}
