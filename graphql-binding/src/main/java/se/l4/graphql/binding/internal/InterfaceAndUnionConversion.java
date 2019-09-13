package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.Types;
import se.l4.commons.types.matching.MatchedTypeRef;
import se.l4.commons.types.matching.TypeMatchingHashMap;
import se.l4.commons.types.matching.TypeMatchingMap;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.annotations.GraphQLConvertFrom;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;

/**
 * Helper for creating conversions between a Java interface and a GraphQL
 * interface.
 */
public class InterfaceAndUnionConversion
{
	private final TypeMatchingMap<Set<TrackedConversion>> types;

	public InterfaceAndUnionConversion()
	{
		types = new TypeMatchingHashMap<>();
	}

	public void trackUnionOrInterface(TypeRef type)
	{
		types.put(type, new HashSet<>());
	}

	public void add(TypeRef input, TypeRef output, DataFetchingConversion<?, ?> conversion)
	{
		List<MatchedTypeRef<Set<TrackedConversion>>> mappingTo = types.getAll(output);
		for(MatchedTypeRef<Set<TrackedConversion>> m : mappingTo)
		{
			/*
			 * For every interface or union we are part of add the
			 * type that can be converted into this GraphQL type.
			 */
			m.getData().add(new TrackedConversion(input, output, conversion));
		}
	}

	/**
	 * Create the resolvers based on the implementations of the interfaces
	 * that have been found.
	 *
	 * @return
	 */
	public List<GraphQLOutputResolver> createResolvers()
	{
		List<GraphQLOutputResolver> result = new ArrayList<>();

		for(MatchedTypeRef<Set<TrackedConversion>> abstractType : types.entries())
		{
			// Build up a list of the types that can be converted into type
			List<TypeRef> types = new ArrayList<>();

			Optional<GraphQLConvertFrom> convertFrom = abstractType.getType()
				.getAnnotation(GraphQLConvertFrom.class);

			if(convertFrom.isPresent())
			{
				for(Class<?> c : convertFrom.get().value())
				{
					types.add(Types.reference(c));
				}
			}

			// Check which conversions fit what type
			Multimap<TypeRef, TrackedConversion>  conversions = HashMultimap.create();

			for(TrackedConversion conversion : abstractType.getData())
			{
				for(TypeRef type : types)
				{
					if(type.isAssignableFrom(conversion.input))
					{
						conversions.put(type, conversion);
					}
				}
			}

			// Register a resolver for every interface
			for(TypeRef interfaceType : types)
			{
				result.add(new Resolver(
					interfaceType,
					abstractType.getType(),
					conversions.get(interfaceType)
				));
			}
		}

		return result;
	}

	private static class TrackedConversion
	{
		private final TypeRef input;
		private final TypeRef output;
		private final DataFetchingConversion<?, ?> conversion;

		public TrackedConversion(
			TypeRef input,
			TypeRef output,
			DataFetchingConversion<?, ?> conversion
		)
		{
			this.input = input.withoutUsage();
			this.output = output.withoutUsage();
			this.conversion = conversion;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((conversion == null) ? 0 : conversion.hashCode());
			result = prime * result + ((input == null) ? 0 : input.hashCode());
			result = prime * result + ((output == null) ? 0 : output.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TrackedConversion other = (TrackedConversion) obj;
			if (conversion == null) {
				if (other.conversion != null)
					return false;
			} else if (!conversion.equals(other.conversion))
				return false;
			if (input == null) {
				if (other.input != null)
					return false;
			} else if (!input.equals(other.input))
				return false;
			if (output == null) {
				if (other.output != null)
					return false;
			} else if (!output.equals(other.output))
				return false;
			return true;
		}
	}

	private static class Resolver
		implements GraphQLOutputResolver
	{
		private final TypeRef interfaceType;
		private final TypeRef graphQLType;
		private final TrackedConversion[] conversions;

		public Resolver(
			TypeRef interfaceType,
			TypeRef graphQLType,
			Collection<TrackedConversion> conversions
		)
		{
			this.interfaceType = interfaceType;
			this.graphQLType = graphQLType;

			this.conversions = conversions.toArray(new TrackedConversion[conversions.size()]);
		}

		@Override
		public boolean supportsOutput(TypeRef type)
		{
			return interfaceType.isAssignableFrom(type) && ! graphQLType.isAssignableFrom(type);
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(
			GraphQLOutputEncounter encounter
		)
		{
			GraphQLResolverContext context = encounter.getContext();
			return context.resolveOutput(graphQLType)
				.withOutputConversion(new FactoryConverter(conversions));
		}

		@Override
		public String toString()
		{
			return "Interface and Union type conversion";
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static class FactoryConverter
		implements DataFetchingConversion<Object, Object>
	{
		private final TrackedConversion[] conversions;

		public FactoryConverter(TrackedConversion[] conversions)
		{
			this.conversions = conversions;
		}

		@Override
		public Object convert(DataFetchingEnvironment environment, Object object)
		{
			if(object == null) return null;

			TypeRef objectType = Types.reference(object.getClass());
			for(TrackedConversion c : conversions)
			{
				if(c.input.isAssignableFrom(objectType))
				{
					return ((DataFetchingConversion) c.conversion).convert(environment, object);
				}
			}

			throw new GraphQLMappingException("Unknown type encountered, register a GraphQL type for the type to support it: " + object.getClass());
		}
	}
}
