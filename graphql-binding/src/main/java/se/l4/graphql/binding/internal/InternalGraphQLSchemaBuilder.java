package se.l4.graphql.binding.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import graphql.Scalars;
import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import se.l4.commons.types.DefaultInstanceFactory;
import se.l4.commons.types.InstanceFactory;
import se.l4.commons.types.Types;
import se.l4.commons.types.conversion.StandardTypeConverter;
import se.l4.commons.types.conversion.TypeConverter;
import se.l4.commons.types.reflect.Annotated;
import se.l4.commons.types.reflect.MemberRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.GraphQLScalar;
import se.l4.graphql.binding.annotations.GraphQLDescription;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLMutation;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.builders.GraphQLObjectBuilderImpl;
import se.l4.graphql.binding.internal.factory.Factory;
import se.l4.graphql.binding.internal.factory.FactoryResolver;
import se.l4.graphql.binding.internal.resolvers.ArrayResolver;
import se.l4.graphql.binding.internal.resolvers.ConvertingTypeResolver;
import se.l4.graphql.binding.internal.resolvers.InputObjectTypeResolver;
import se.l4.graphql.binding.internal.resolvers.ListResolver;
import se.l4.graphql.binding.internal.resolvers.ObjectTypeResolver;
import se.l4.graphql.binding.internal.resolvers.ScalarResolver;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.query.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

/**
 * Internal class that does the actual resolving of types.
 */
public class InternalGraphQLSchemaBuilder
{
	private final NameRegistry names;
	private final TypeResolverRegistry typeResolvers;

	private final Map<Class<?>, Supplier<?>> rootTypes;
	private final List<Class<?>> types;

	private final Map<TypeRef, ResolvedGraphQLType<? extends GraphQLOutputType>> builtOutputTypes;
	private final Map<TypeRef, ResolvedGraphQLType<? extends GraphQLInputType>> builtInputTypes;

	private final TypeConverter typeConverter;

	private InstanceFactory instanceFactory;

	public InternalGraphQLSchemaBuilder()
	{
		instanceFactory = new DefaultInstanceFactory();

		names = new NameRegistry();
		typeResolvers = new TypeResolverRegistry();

		rootTypes = new HashMap<>();
		types = new ArrayList<>();

		builtInputTypes = new HashMap<>();
		builtOutputTypes = new HashMap<>();

		typeConverter = new StandardTypeConverter();
		typeConverter.addConversion(StringValue.class, String.class, value -> value.getValue());
		typeConverter.addConversion(BooleanValue.class, Boolean.class, value -> value.isValue());
		typeConverter.addConversion(IntValue.class, BigInteger.class, value -> value.getValue());
		typeConverter.addConversion(FloatValue.class, BigDecimal.class, value -> value.getValue());

		// Register the built-in scalars
		registerBuiltin(Scalars.GraphQLString, String.class);
		registerBuiltin(Scalars.GraphQLChar, char.class, Character.class);

		registerBuiltin(Scalars.GraphQLBoolean, boolean.class, Boolean.class);

		registerBuiltin(Scalars.GraphQLByte, byte.class, Byte.class);
		registerBuiltin(Scalars.GraphQLShort, short.class, Short.class);
		registerBuiltin(Scalars.GraphQLInt, int.class, Integer.class);
		registerBuiltin(Scalars.GraphQLLong, long.class, Long.class);
		registerBuiltin(Scalars.GraphQLFloat, float.class, Float.class, double.class, Double.class);

		registerBuiltin(Scalars.GraphQLBigInteger, BigInteger.class);
		registerBuiltin(Scalars.GraphQLBigDecimal, BigDecimal.class);

		// Register some default type converters
		typeResolvers.bindAny(Collection.class, new ListResolver());
		typeResolvers.bindAny(Object.class, new ArrayResolver());
		typeResolvers.bindAny(Object.class, new InputObjectTypeResolver());
		typeResolvers.bindAny(Object.class, new ObjectTypeResolver());
	}

	/**
	 * Register a built in scalar and bind it to the specified types.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerBuiltin(GraphQLScalarType graphQLType, Class<?>... types)
	{
		ResolvedGraphQLType<?> resolved = ResolvedGraphQLType.forType(graphQLType);

		for(Class<?> t : types)
		{
			TypeRef type = Types.reference(t);

			builtOutputTypes.put(type, (ResolvedGraphQLType) resolved);
			builtInputTypes.put(type, (ResolvedGraphQLType) resolved);
		}

		names.reserveName(graphQLType.getName(), Breadcrumb.custom("by built-in scalar " + graphQLType.getName()));
	}

	public void setInstanceFactory(InstanceFactory instanceFactory)
	{
		this.instanceFactory = instanceFactory;
	}

	/**
	 * Add a type that should be used to extract queries and mutations in the
	 * root of the system.
	 *
	 * @param type
	 * @param supplier
	 */
	public void addRootType(Class<?> type, Supplier<?> supplier)
	{
		this.rootTypes.put(type, supplier);
	}

	/**
	 * Add a scalar binding.
	 *
	 * @param <JavaType>
	 * @param type
	 * @param scalar
	 */
	public <JavaType> void addScalar(Class<JavaType> type, GraphQLScalar<JavaType, ?> scalar)
	{
		this.typeResolvers.bindAny(type, new ScalarResolver(scalar));
	}

	/**
	 * Add a type that should be made available.
	 *
	 * @param type
	 */
	public void addType(Class<?> type)
	{
		this.types.add(type);
	}

	public void bind(Class<?> type, GraphQLResolver resolver)
	{
		this.typeResolvers.bindAny(type, resolver);
	}

	/**
	 * Build the root query type by going through and resolving all of the root
	 * types.
	 *
	 * @param ctx
	 * @return
	 */
	private GraphQLObjectType buildRootQuery(ResolverContextImpl ctx)
	{
		GraphQLObjectBuilderImpl builder = new GraphQLObjectBuilderImpl(
			ctx,
			ctx.codeRegistryBuilder
		);

		builder.setName("Query");

		for(Map.Entry<Class<?>, Supplier<?>> e : rootTypes.entrySet())
		{
			Supplier<?> supplier = e.getValue();
			ObjectTypeResolver.resolve(
				ctx,
				Types.reference(e.getKey()),
				env -> supplier.get(),
				builder,
				GraphQLField.class
			);
		}

		return builder.build();
	}

	private GraphQLObjectType buildMutation(ResolverContextImpl ctx)
	{
		GraphQLObjectBuilderImpl builder = new GraphQLObjectBuilderImpl(
			ctx,
			ctx.codeRegistryBuilder
		);

		builder.setName("Mutation");

		for(Map.Entry<Class<?>, Supplier<?>> e : rootTypes.entrySet())
		{
			Supplier<?> supplier = e.getValue();
			ObjectTypeResolver.resolve(
				ctx,
				Types.reference(e.getKey()),
				env -> supplier.get(),
				builder,
				GraphQLMutation.class
			);
		}

		return builder.build();
	}

	public GraphQLSchema build()
	{
		GraphQLSchema.Builder builder = GraphQLSchema.newSchema();
		GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

		ResolverContextImpl ctx = new ResolverContextImpl(
			codeRegistryBuilder
		);

		// Resolve all of the extra bindings - for when types use @GraphQLSource
		for(Class<?> type : types)
		{
			List<Factory<Object, ?>> factories = FactoryResolver.resolveFactories(ctx, Types.reference(type));
			for(Factory<Object, ?> factory : factories)
			{
				typeResolvers.bindOutput(factory.getInput(), new ConvertingTypeResolver<>(factory));
			}
		}

		// Resolve all of the known types
		for(Class<?> type : types)
		{
			TypeRef typeRef = Types.reference(type);

			ResolvedGraphQLType<?> output = ctx.maybeResolveOutput(typeRef);
			if(output.isPresent())
			{
				builder.additionalType(output.getGraphQLType());
			}

			ResolvedGraphQLType<?> input = ctx.maybeResolveInput(typeRef);
			if(input.isPresent())
			{
				builder.additionalType(input.getGraphQLType());
			}
		}

		// Build the root type
		builder.query(buildRootQuery(ctx));

		// Build the mutation type
		builder.mutation(buildMutation(ctx));

		return builder.codeRegistry(codeRegistryBuilder.build())
			.build();
	}

	private class ResolverContextImpl
		implements GraphQLResolverContext
	{
		protected final GraphQLCodeRegistry.Builder codeRegistryBuilder;

		private final Set<TypeRef> inputsBeingResolved;
		private final Map<TypeRef, PendingDataFetchingConversion<?, ?>> pendingInputConversions;

		private final Set<TypeRef> outputsBeingResolved;
		private final Map<TypeRef, PendingDataFetchingConversion<?, ?>> pendingOutputConversions;

		private Breadcrumb breadcrumb;

		public ResolverContextImpl(
			GraphQLCodeRegistry.Builder codeRegistryBuilder
		)
		{
			this.codeRegistryBuilder = codeRegistryBuilder;

			breadcrumb = Breadcrumb.empty();

			inputsBeingResolved = new HashSet<>();
			pendingInputConversions = new HashMap<>();

			outputsBeingResolved = new HashSet<>();
			pendingOutputConversions = new HashMap<>();
		}

		@Override
		public InstanceFactory getInstanceFactory()
		{
			return instanceFactory;
		}

		@Override
		public TypeConverter getTypeConverter()
		{
			return typeConverter;
		}

		@Override
		public Breadcrumb getBreadcrumb()
		{
			return breadcrumb;
		}

		@Override
		public void breadcrumb(Breadcrumb crumb, Runnable runnable)
		{
			Breadcrumb current = breadcrumb;
			breadcrumb = current.then(crumb);

			try
			{
				runnable.run();
			}
			finally
			{
				breadcrumb = current;
			}
		}

		@Override
		public <T> T breadcrumb(Breadcrumb crumb, Supplier<T> supplier)
		{
			Breadcrumb current = breadcrumb;
			breadcrumb = current.then(crumb);

			try
			{
				return supplier.get();
			}
			finally
			{
				breadcrumb = current;
			}
		}

		@Override
		public GraphQLMappingException newError(String message, Object... args)
		{
			return new GraphQLMappingException(String.format(message, args) + "\n  " + breadcrumb.getLocation());
		}

		@Override
		public GraphQLMappingException newError(Breadcrumb crumb, String message, Object... args)
		{
			return new GraphQLMappingException(String.format(message, args) + "\n  " + breadcrumb.then(crumb).getLocation());
		}

		@Override
		public String getTypeName(TypeRef type)
		{
			try
			{
				return names.getName(type);
			}
			catch(GraphQLMappingException e)
			{
				throw new GraphQLMappingException(e.getMessage() + "\n  " + breadcrumb.getLocation());
			}
		}

		@Override
		public boolean hasTypeName(String name)
		{
			return names.hasName(name);
		}

		@Override
		public void requestTypeName(String name)
		{
			try
			{
				names.reserveName(name, breadcrumb);
			}
			catch(GraphQLMappingException e)
			{
				throw new GraphQLMappingException(e.getMessage() + "\n  " + breadcrumb.getLocation());
			}
		}

		@Override
		public String getMemberName(MemberRef member)
		{
			try
			{
				return names.getName(member);
			}
			catch(GraphQLMappingException e)
			{
				throw new GraphQLMappingException(e.getMessage() + "\n  " + breadcrumb.getLocation());
			}
		}

		@Override
		public String getParameterName(ParameterRef parameter)
		{
			try
			{
				return names.getName(parameter);
			}
			catch(GraphQLMappingException e)
			{
				throw new GraphQLMappingException(e.getMessage() + "\n  " + breadcrumb.getLocation());
			}
		}

		@Override
		public String getDescription(Annotated annotated)
		{
			return annotated.findAnnotation(GraphQLDescription.class)
				.map(GraphQLDescription::value)
				.orElse("");
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void registerResolved(TypeRef type, ResolvedGraphQLType<?> resolved)
		{
			GraphQLType gql = resolved.getGraphQLType();
			if(gql instanceof GraphQLModifiedType)
			{
				gql = ((GraphQLModifiedType) gql).getWrappedType();
			}

			if(gql instanceof GraphQLInputType)
			{
				builtInputTypes.put(type, (ResolvedGraphQLType) resolved);
			}

			if(gql instanceof GraphQLOutputType)
			{
				builtOutputTypes.put(type, (ResolvedGraphQLType) resolved);
			}
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLOutputType> maybeResolveOutput(TypeRef type)
		{
			TypeRef withoutUsage = type.withoutUsage();

			ResolvedGraphQLType<? extends GraphQLOutputType> graphQLType;
			if(builtOutputTypes.containsKey(withoutUsage))
			{
				graphQLType = builtOutputTypes.get(withoutUsage);
			}
			else
			{
				if(outputsBeingResolved.contains(withoutUsage))
				{
					/*
					 * This type is already being resolved, return a type
					 * reference with a pending conversion.
					 */

					String name = getTypeName(withoutUsage);
					PendingDataFetchingConversion<?, ?> pending = pendingOutputConversions
						.computeIfAbsent(withoutUsage, (key) -> new PendingDataFetchingConversion<>());

					return ResolvedGraphQLType.forType(GraphQLTypeReference.typeRef(name))
						.withConversion(pending);
				}

				try
				{
					outputsBeingResolved.add(withoutUsage);

					Optional<GraphQLOutputResolver> resolver = typeResolvers.getOutputResolver(type.getErasedType());
					if(! resolver.isPresent())
					{
						return ResolvedGraphQLType.none();
					}

					graphQLType = resolver.get().resolveOutput(new OutputEncounterImpl(
						this,
						withoutUsage
					));
				}
				finally
				{
					outputsBeingResolved.remove(withoutUsage);
				}

				if(! graphQLType.isPresent())
				{
					return ResolvedGraphQLType.none();
				}

				PendingDataFetchingConversion<?, ?> pending = pendingOutputConversions.get(withoutUsage);
				if(pending != null)
				{
					pending.update(graphQLType.getConversion());
				}

				registerResolved(withoutUsage, graphQLType);
			}

			if(type.getUsage().hasAnnotation(GraphQLNonNull.class))
			{
				graphQLType = graphQLType.nonNull();
			}

			return graphQLType;
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(TypeRef type)
		{
			ResolvedGraphQLType<? extends GraphQLOutputType> resolved = maybeResolveOutput(type);
			if(resolved.isPresent())
			{
				return resolved;

			}

			throw new GraphQLMappingException(
				"Requested output binding of `" + type.toTypeName()
				+ "` but type is not resolvable to a GraphQL type. "
				+ "Types need to be annotated or bound via resolvers"
			);
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLInputType> maybeResolveInput(TypeRef type)
		{
			TypeRef withoutUsage = type.withoutUsage();

			ResolvedGraphQLType<? extends GraphQLInputType> graphQLType;
			if(builtInputTypes.containsKey(withoutUsage))
			{
				graphQLType = builtInputTypes.get(withoutUsage);
			}
			else
			{
				if(inputsBeingResolved.contains(withoutUsage))
				{
					/*
					 * This type is already being resolved, return a type
					 * reference with a pending conversion.
					 */

					String name = getTypeName(withoutUsage);
					PendingDataFetchingConversion<?, ?> pending = pendingInputConversions
						.computeIfAbsent(withoutUsage, (key) -> new PendingDataFetchingConversion<>());

					return ResolvedGraphQLType.forType(GraphQLTypeReference.typeRef(name))
						.withConversion(pending);
				}

				try
				{
					inputsBeingResolved.add(withoutUsage);

					Optional<GraphQLInputResolver> resolver = typeResolvers.getInputResolver(type.getErasedType());
					if(! resolver.isPresent())
					{
						return ResolvedGraphQLType.none();
					}

					graphQLType = resolver.get().resolveInput(new InputEncounterImpl(
						this,
						withoutUsage
					));
				}
				finally
				{
					inputsBeingResolved.remove(withoutUsage);
				}

				if(! graphQLType.isPresent())
				{
					return ResolvedGraphQLType.none();
				}

				PendingDataFetchingConversion<?, ?> pending = pendingInputConversions.get(withoutUsage);
				if(pending != null)
				{
					pending.update(graphQLType.getConversion());
				}

				registerResolved(withoutUsage, graphQLType);
			}

			if(type.getUsage().hasAnnotation(GraphQLNonNull.class))
			{
				graphQLType = graphQLType.nonNull();
			}

			return graphQLType;
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(TypeRef type)
		{
			ResolvedGraphQLType<? extends GraphQLInputType> resolved = maybeResolveInput(type);
			if(resolved.isPresent())
			{
				return resolved;

			}

			throw new GraphQLMappingException(
				"Requested input binding of `" + type.toTypeName()
				+ "` but type is not resolvable to a GraphQL type. "
				+ "Types need to be annotated or bound via resolvers"
			);
		}
	}

	private class OutputEncounterImpl
		implements GraphQLOutputEncounter
	{
		private final ResolverContextImpl context;
		private final TypeRef type;

		public OutputEncounterImpl(
			ResolverContextImpl context,
			TypeRef type
		)
		{
			this.context = context;
			this.type = type;
		}

		public GraphQLResolverContext getContext()
		{
			return context;
		}

		@Override
		public TypeRef getType()
		{
			return type;
		}

		@Override
		public GraphQLObjectBuilder newObjectType()
		{
			return new GraphQLObjectBuilderImpl(
				context,
				context.codeRegistryBuilder
			);
		}
	}

	private class InputEncounterImpl
		implements GraphQLInputEncounter
	{
		private final ResolverContextImpl context;
		private final TypeRef type;

		public InputEncounterImpl(
			ResolverContextImpl context,
			TypeRef type
		)
		{
			this.context = context;
			this.type = type;
		}

		@Override
		public GraphQLResolverContext getContext()
		{
			return context;
		}

		@Override
		public TypeRef getType()
		{
			return type;
		}

	}
}
