package se.l4.graphql.binding.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.Types;
import se.l4.commons.types.matching.ClassMatchingHashMap;
import se.l4.commons.types.matching.ClassMatchingMap;
import se.l4.commons.types.matching.MatchedType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.internal.factory.Factory;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Helper for creating conversions between a Java interface and a GraphQL
 * interface.
 */
public class InterfaceAndUnionConversion
{
	private final ClassMatchingMap<Object, Set<Factory<?, ?>>> types;

	public InterfaceAndUnionConversion()
	{
		types = new ClassMatchingHashMap<>();
	}

	public void trackUnionOrInterface(Class<?> type)
	{
		types.put(type, new HashSet<>());
	}

	public void addFactory(Factory<?, ?> factory)
	{
		List<MatchedType<Object, Set<Factory<?, ?>>>> mappingTo = types.getAll(factory.getOutput().getErasedType());
		for(MatchedType<Object, Set<Factory<?, ?>>> m : mappingTo)
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

		for(MatchedType<Object, Set<Factory<?, ?>>> type : types.entries())
		{
			// Figure out what interfaces are shared between all the factories
			Set<Class<?>> sharedInterfaces = null;
			for(Factory<?, ?> factory : type.getData())
			{
				Set<Class<?>> interfaces = new HashSet<>();
				factory.getInput().visitHierarchy(t -> {
					if(t.isInterface())
					{
						interfaces.add(t.getErasedType());
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
				for(Class<?> interfaceType : sharedInterfaces)
				{
					result.add(new Resolver(interfaceType, type.getType(), type.getData()));
				}
			}
		}

		return result;
	}

	private static class Resolver
		implements TypedGraphQLOutputResolver
	{
		private final Class<?> interfaceType;
		private final Class<?> graphQLType;
		private final Factory<?, ?>[] factories;

		public Resolver(
			Class<?> interfaceType,
			Class<?> graphQLType,
			Set<Factory<?, ?>> factories
		)
		{
			this.interfaceType = interfaceType;
			this.graphQLType = graphQLType;

			this.factories = factories.toArray(new Factory[factories.size()]);
		}

		@Override
		public Class<?> getType()
		{
			return interfaceType;
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(
			GraphQLOutputEncounter encounter
		)
		{
			GraphQLResolverContext context = encounter.getContext();
			return context.resolveOutput(Types.reference(graphQLType))
				.withOutputConversion(new FactoryConverter(factories));
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
