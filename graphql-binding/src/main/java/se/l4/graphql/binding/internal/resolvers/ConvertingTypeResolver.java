package se.l4.graphql.binding.internal.resolvers;

import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.Types;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.internal.factory.Factory;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;

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
	public boolean supportsOutput(TypeRef type)
	{
		return type.getErasedType() == factory.getInput();
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();

		ResolvedGraphQLType<? extends GraphQLOutputType> type = context.resolveOutput(
			Types.reference(factory.getOutput())
		);

		// Get the type but apply a conversion to our type
		return type.withOutputConversion((DataFetchingConversion) factory);
	}

}
