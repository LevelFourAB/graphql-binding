package se.l4.graphql.binding.internal.resolvers;

import java.util.Optional;

import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.Types;
import se.l4.commons.types.reflect.TypeInferrer;
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
	private final TypeRef from;
	private final DataFetchingConversion<I, O> conversion;

	private final TypeInferrer[] parameterUsageInferrers;
	private final TypeInferrer typeParameterInferrer;

	public ConvertingTypeResolver(
		TypeRef from,
		TypeRef to,
		DataFetchingConversion<I, O> conversion
	)
	{
		this.from = from;
		this.conversion = conversion;

		TypeRef type;
		if(conversion instanceof Factory)
		{
			type = to;
		}
		else
		{
			type = Types.reference(conversion.getClass());
		}


		TypeInferrer[] parameterUsageInferrers = new TypeInferrer[type.getTypeParameterCount()];
		for(int i=0, n=parameterUsageInferrers.length; i<n; i++)
		{
			parameterUsageInferrers[i] = type.getTypeParameterUsageInferrer(i, from);
		}

		this.parameterUsageInferrers = parameterUsageInferrers;
		this.typeParameterInferrer = type.getTypeParameterInferrer(to);
	}

	@Override
	public boolean supportsOutput(TypeRef type)
	{
		return from.isAssignableFrom(type);
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();

		TypeRef in = encounter.getType();

		TypeRef[] refs = new TypeRef[parameterUsageInferrers.length];
		for(int i=0, n=refs.length; i<n; i++)
		{
			Optional<TypeRef> inferred = parameterUsageInferrers[i].infer(in);
			if(! inferred.isPresent())
			{
				return ResolvedGraphQLType.none();
			}

			refs[i] = inferred.get();
		}

		Optional<TypeRef> to = typeParameterInferrer.infer(refs);

		if(! to.isPresent())
		{
			return ResolvedGraphQLType.none();
		}

		ResolvedGraphQLType<? extends GraphQLOutputType> type = context.resolveOutput(to.get());

		// Get the type but apply a conversion to our type
		return type.withOutputConversion((DataFetchingConversion) conversion);
	}

	@Override
	public String toString()
	{
		return "conversion " + conversion.getClass().getSimpleName();
	}
}
