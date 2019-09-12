package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.Types;
import se.l4.commons.types.matching.MatchedTypeRef;
import se.l4.commons.types.matching.TypeMatchingHashMap;
import se.l4.commons.types.matching.TypeMatchingMap;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.internal.factory.Factory;
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
	private final TypeMatchingMap<Set<Factory<?, ?>>> types;

	public InterfaceAndUnionConversion()
	{
		types = new TypeMatchingHashMap<>();
	}

	public void trackUnionOrInterface(TypeRef type)
	{
		types.put(type, new HashSet<>());
	}

	public void addFactory(Factory<?, ?> factory)
	{
		List<MatchedTypeRef<Set<Factory<?, ?>>>> mappingTo = types.getAll(factory.getOutput());
		for(MatchedTypeRef<Set<Factory<?, ?>>> m : mappingTo)
		{
			/*
			 * For every interface or union we are part of add the
			 * type that can be converted into this GraphQL type.
			 */
			m.getData().add(factory);
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

		for(MatchedTypeRef<Set<Factory<?, ?>>> type : types.entries())
		{
			// Figure out what interfaces are shared between all the factories
			Set<TypeRef> sharedInterfaces = null;
			for(Factory<?, ?> factory : type.getData())
			{
				Set<TypeRef> interfaces = new HashSet<>();
				factory.getInput().visitHierarchy(t -> {
					if(t.isInterface())
					{
						interfaces.add(t.withoutUsage());
					}

					return true;
				});

				if(sharedInterfaces == null)
				{
					sharedInterfaces = interfaces;
				}
				else
				{
					sharedInterfaces.retainAll(interfaces);
				}
			}

			// Register a resolver for every interface
			if(sharedInterfaces != null)
			{
				for(TypeRef interfaceType : sharedInterfaces)
				{
					result.add(new Resolver(interfaceType, type.getType(), type.getData()));
				}
			}
		}

		return result;
	}

	private static class Resolver
		implements GraphQLOutputResolver
	{
		private final TypeRef interfaceType;
		private final TypeRef graphQLType;
		private final Factory<?, ?>[] factories;

		public Resolver(
			TypeRef interfaceType,
			TypeRef graphQLType,
			Set<Factory<?, ?>> factories
		)
		{
			this.interfaceType = interfaceType;
			this.graphQLType = graphQLType;

			this.factories = factories.toArray(new Factory[factories.size()]);
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
				.withOutputConversion(new FactoryConverter(factories));
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
		private final Factory[] factories;

		public FactoryConverter(Factory[] factories)
		{
			this.factories = factories;
		}

		@Override
		public Object convert(DataFetchingEnvironment environment, Object object)
		{
			if(object == null) return null;

			TypeRef objectType = Types.reference(object.getClass());
			for(Factory factory : factories)
			{
				if(factory.getInput().isAssignableFrom(objectType))
				{
					return factory.convert(environment, object);
				}
			}

			throw new GraphQLMappingException("Unknown type encountered, register a GraphQL type for the type to support it: " + object.getClass());
		}
	}
}
