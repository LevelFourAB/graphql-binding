package se.l4.graphql.binding.internal;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Defaults;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
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
import se.l4.graphql.binding.annotations.GraphQLContext;
import se.l4.graphql.binding.annotations.GraphQLDescription;
import se.l4.graphql.binding.annotations.GraphQLEnvironment;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLMutation;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLUnion;
import se.l4.graphql.binding.internal.builders.GraphQLInterfaceBuilderImpl;
import se.l4.graphql.binding.internal.builders.GraphQLObjectBuilderImpl;
import se.l4.graphql.binding.internal.builders.GraphQLUnionBuilderImpl;
import se.l4.graphql.binding.internal.directive.GraphQLDirectiveCreationEncounterImpl;
import se.l4.graphql.binding.internal.directive.GraphQLDirectiveFieldEncounterImpl;
import se.l4.graphql.binding.internal.factory.Factory;
import se.l4.graphql.binding.internal.factory.FactoryResolver;
import se.l4.graphql.binding.internal.parameters.GraphQLContextParameterResolver;
import se.l4.graphql.binding.internal.parameters.GraphQLEnvironmentParameterResolver;
import se.l4.graphql.binding.internal.resolvers.ArrayResolver;
import se.l4.graphql.binding.internal.resolvers.ConvertingTypeResolver;
import se.l4.graphql.binding.internal.resolvers.EnumResolver;
import se.l4.graphql.binding.internal.resolvers.InputObjectTypeResolver;
import se.l4.graphql.binding.internal.resolvers.InterfaceResolver;
import se.l4.graphql.binding.internal.resolvers.IterableResolver;
import se.l4.graphql.binding.internal.resolvers.ObjectTypeResolver;
import se.l4.graphql.binding.internal.resolvers.OptionalDoubleResolver;
import se.l4.graphql.binding.internal.resolvers.OptionalIntResolver;
import se.l4.graphql.binding.internal.resolvers.OptionalLongResolver;
import se.l4.graphql.binding.internal.resolvers.OptionalResolver;
import se.l4.graphql.binding.internal.resolvers.ScalarResolver;
import se.l4.graphql.binding.internal.resolvers.SpecificScalarResolver;
import se.l4.graphql.binding.internal.resolvers.UnionResolver;
import se.l4.graphql.binding.naming.DefaultGraphQLNamingFunction;
import se.l4.graphql.binding.naming.GraphQLNamingEncounter;
import se.l4.graphql.binding.naming.GraphQLNamingFunction;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLConversion;
import se.l4.graphql.binding.resolver.GraphQLParameterEncounter;
import se.l4.graphql.binding.resolver.GraphQLParameterResolver;
import se.l4.graphql.binding.resolver.GraphQLResolver;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.GraphQLScalarResolver;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldResolver;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldResult;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveResolver;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLInterfaceBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLObjectMixin;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLUnionBuilder;

/**
 * Internal class that does the actual resolving of types.
 */
public class InternalGraphQLSchemaBuilder
{
	private final NameRegistry names;
	private final TypeResolverRegistry typeResolvers;

	private final Map<Class<?>, DataFetchingSupplier<?>> rootTypes;
	private final List<Class<?>> types;

	private final Map<TypeRef, ResolvedGraphQLType<? extends GraphQLOutputType>> builtOutputTypes;
	private final Map<TypeRef, ResolvedGraphQLType<? extends GraphQLInputType>> builtInputTypes;
	private final Map<Class<?>, GraphQLDirective> builtDirectives;

	private final TypeConverter typeConverter;
	private final List<GraphQLObjectMixin> objectMixins;
	private final Map<Class<? extends Annotation>, GraphQLDirectiveResolver<? extends Annotation>> directives;

	private final Map<Class<? extends Annotation>, GraphQLParameterResolver<?>> parameterResolvers;

	private InstanceFactory instanceFactory;
	private GraphQLNamingFunction defaultNaming;

	public InternalGraphQLSchemaBuilder()
	{
		instanceFactory = new DefaultInstanceFactory();

		names = new NameRegistry();
		typeResolvers = new TypeResolverRegistry();
		defaultNaming = new DefaultGraphQLNamingFunction();

		parameterResolvers = new HashMap<>();

		rootTypes = new HashMap<>();
		types = new ArrayList<>();
		objectMixins = new ArrayList<>();
		directives = new HashMap<>();

		builtInputTypes = new HashMap<>();
		builtOutputTypes = new HashMap<>();
		builtDirectives =new HashMap<>();

		typeConverter = new StandardTypeConverter();

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

		// Register default parameters
		parameterResolvers.put(GraphQLEnvironment.class, new GraphQLEnvironmentParameterResolver());
		parameterResolvers.put(GraphQLContext.class, new GraphQLContextParameterResolver());
	}

	/**
	 * Register a built in scalar and bind it to the specified types.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerBuiltin(GraphQLScalarType graphQLType, Class<?>... classes)
	{
		ResolvedGraphQLType<?> resolved = ResolvedGraphQLType.forType(graphQLType);

		TypeRef[] types = Arrays.stream(classes)
			.map(t -> Types.reference(t))
			.toArray(TypeRef[]::new);

		for(TypeRef type : types)
		{
			Object defaultValue = Defaults.defaultValue(type.getErasedType());
			ResolvedGraphQLType<?> withDefault = resolved.withDefaultValue(env -> defaultValue);

			builtOutputTypes.put(type, (ResolvedGraphQLType) withDefault);
			builtInputTypes.put(type, (ResolvedGraphQLType) withDefault);
		}

		names.reserveName(
			graphQLType.getName(),
			Breadcrumb.custom("by built-in scalar " + graphQLType.getName()),
			types
		);
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
	public void addRootType(Class<?> type, DataFetchingSupplier<?> supplier)
	{
		this.rootTypes.put(type, supplier);
		this.objectMixins.add(new RootObjectMixin(
			Types.reference(type),
			supplier
		));
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

	public void addResolver(GraphQLResolver resolver)
	{
		this.typeResolvers.add(resolver);

		if(resolver instanceof GraphQLConversion)
		{
			addConversionResolver((GraphQLConversion<?, ?>) resolver);
		}

		if(resolver instanceof GraphQLScalarResolver)
		{
			addScalar((GraphQLScalarResolver<?, ?>) resolver);
		}

		if(resolver instanceof GraphQLParameterResolver)
		{
			addParameter((GraphQLParameterResolver<?>) resolver);
		}
	}

	private void addConversionResolver(GraphQLConversion<?, ?> conversion)
	{
		TypeRef conversionType = Types.reference(conversion.getClass())
			.findInterface(GraphQLConversion.class)
			.get();

		TypeRef from = conversionType.getTypeParameter(0)
			.orElseThrow(() -> new GraphQLMappingException("Could not find type of annotation"));

		TypeRef to = conversionType.getTypeParameter(1)
			.orElseThrow(() -> new GraphQLMappingException("Could not find type of annotation"));

		this.typeResolvers.add(new ConvertingTypeResolver<>(from, to, conversion));
	}

	/**
	 * Add a scalar binding.
	 *
	 * @param scalar
	 */
	private void addScalar(GraphQLScalarResolver<?, ?> scalar)
	{
		// Resolve the interface and the GraphQL type and request a conversion to it
		TypeRef scalarInterface = Types.reference(scalar.getClass())
			.getInterface(GraphQLScalarResolver.class)
			.get();

		TypeRef javaType = scalarInterface.getTypeParameter(0).get();
		TypeRef graphQLType = scalarInterface.getTypeParameter(1).get();

		this.typeResolvers.add(new SpecificScalarResolver(
			javaType,
			graphQLType,
			scalar
		));
	}

	/**
	 * Add a directive for use during resolution.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addDirective(GraphQLDirectiveResolver<? extends Annotation> directive)
	{
		TypeRef directiveResolverType = Types.reference(directive.getClass())
			.findInterface(GraphQLDirectiveResolver.class)
			.get();

		TypeRef annotation = directiveResolverType.getTypeParameter(0)
			.filter(p -> p.isFullyResolved())
			.orElseThrow(() -> new GraphQLMappingException("Could not find type of annotation"));

		this.directives.put((Class) annotation.getErasedType(), directive);
	}

	/**
	 * Add a parameter for use during resolution.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addParameter(GraphQLParameterResolver<? extends Annotation> resolver)
	{
		TypeRef resolverType = Types.reference(resolver.getClass())
			.findInterface(GraphQLParameterResolver.class)
			.get();

		TypeRef annotation = resolverType.getTypeParameter(0)
			.filter(p -> p.isFullyResolved())
			.orElseThrow(() -> new GraphQLMappingException("Could not find type of annotation"));

		this.parameterResolvers.put((Class) annotation.getErasedType(), resolver);
	}

	/**
	 * Build the root query type by going through and resolving all of the root
	 * types.
	 *
	 * @param ctx
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private GraphQLObjectType buildRootQuery(ResolverContextImpl ctx)
	{
		GraphQLObjectBuilderImpl builder = new GraphQLObjectBuilderImpl(
			Collections.emptyList(),
			ctx,
			ctx.codeRegistryBuilder
		);

		builder.setName("Query");

		for(Map.Entry<Class<?>, DataFetchingSupplier<?>> e : rootTypes.entrySet())
		{
			DataFetchingSupplier supplier = e.getValue();
			TypeRef typeRef = Types.reference(e.getKey());

			ctx.breadcrumb(Breadcrumb.forType(typeRef), () -> {
				ObjectTypeResolver.resolve(
					ctx,
					typeRef,
					supplier,
					builder,
					GraphQLField.class
				);
			});
		}

		return builder.build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private GraphQLObjectType buildMutation(ResolverContextImpl ctx)
	{
		GraphQLObjectBuilderImpl builder = new GraphQLObjectBuilderImpl(
			Collections.emptyList(),
			ctx,
			ctx.codeRegistryBuilder
		);

		builder.setName("Mutation");

		for(Map.Entry<Class<?>, DataFetchingSupplier<?>> e : rootTypes.entrySet())
		{
			DataFetchingSupplier supplier = e.getValue();
			TypeRef typeRef = Types.reference(e.getKey());

			ctx.breadcrumb(Breadcrumb.forType(typeRef), () -> {
				ObjectTypeResolver.resolve(
					ctx,
					typeRef,
					supplier,
					builder,
					GraphQLMutation.class
				);
			});
		}

		return builder.build();
	}

	public GraphQLSchema build()
	{
		GraphQLSchema.Builder builder = GraphQLSchema.newSchema();
		GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

		ResolverContextImpl ctx = new ResolverContextImpl(
			builder,
			codeRegistryBuilder
		);

		// Register the default resolvers that can work on any type using annotations
		typeResolvers.add(new InputObjectTypeResolver());
		typeResolvers.add(new ObjectTypeResolver());
		typeResolvers.add(new ScalarResolver());

		typeResolvers.add(new InterfaceResolver());
		typeResolvers.add(new UnionResolver());

		typeResolvers.add(new EnumResolver());

		/*
		 * Find all the interfaces and unions we are interested in keeping
		 * track of conversions for.
		 */
		InterfaceAndUnionConversion interfacesAndUnions = new InterfaceAndUnionConversion();
		for(Class<?> type : types)
		{
			if(type.isAnnotationPresent(GraphQLInterface.class)
				|| type.isAnnotationPresent(GraphQLUnion.class))
			{
				interfacesAndUnions.trackUnionOrInterface(Types.reference(type));
			}
		}

		// Resolve all of the extra bindings - for when types use @GraphQLSource
		for(Class<?> type : types)
		{
			TypeRef typeRef = Types.reference(type);
			ctx.breadcrumb(Breadcrumb.forType(typeRef), () -> {
				List<Factory<?, ?>> factories = FactoryResolver.resolveFactories(ctx, typeRef);
				for(Factory<?, ?> factory : factories)
				{
					// Register an extra resolver for the type
					typeResolvers.add(new ConvertingTypeResolver<>(
						factory.getInput(),
						factory.getOutput(),
						factory
					));

					// Track this factory - for automatic interface and union conversion
					interfacesAndUnions.add(factory.getInput(), factory.getOutput(), factory);
				}
			});
		}

		// Bind the interface and union resolvers
		for(GraphQLOutputResolver resolver : interfacesAndUnions.createResolvers())
		{
			typeResolvers.add(resolver);
		}

		// Register some default type converters that use types to work
		typeResolvers.add(new IterableResolver());
		typeResolvers.add(new ArrayResolver());

		typeResolvers.add(new OptionalResolver());
		typeResolvers.add(new OptionalIntResolver());
		typeResolvers.add(new OptionalLongResolver());
		typeResolvers.add(new OptionalDoubleResolver());

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
		private final GraphQLSchema.Builder schemaBuilder;
		private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

		private final Set<TypeRef> inputsBeingResolved;
		private final Map<TypeRef, PendingDataFetchingConversion<?, ?>> pendingInputConversions;

		private final Set<TypeRef> outputsBeingResolved;
		private final Map<TypeRef, PendingDataFetchingConversion<?, ?>> pendingOutputConversions;

		private final Map<Class<?>, Map<Class<?>, Annotation>> annotationCache;

		private Breadcrumb breadcrumb;

		public ResolverContextImpl(
			GraphQLSchema.Builder schemaBuilder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder
		)
		{
			this.schemaBuilder = schemaBuilder;
			this.codeRegistryBuilder = codeRegistryBuilder;

			breadcrumb = Breadcrumb.empty();

			inputsBeingResolved = new HashSet<>();
			pendingInputConversions = new HashMap<>();

			outputsBeingResolved = new HashSet<>();
			pendingOutputConversions = new HashMap<>();

			annotationCache = new HashMap<>();
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
		public GraphQLMappingException newError(String message)
		{
			return new GraphQLMappingException(message + "\n  " + breadcrumb.getLocation());
		}

		@Override
		public GraphQLMappingException newError(Breadcrumb crumb, String message)
		{
			return new GraphQLMappingException(message + "\n  " + breadcrumb.then(crumb).getLocation());
		}

		@Override
		public Optional<String> getInputTypeName(TypeRef type)
		{
			return getTypeName(type, inputsBeingResolved, this::maybeResolveInput);
		}

		@Override
		public Optional<String> getOutputTypeName(TypeRef type)
		{
			return getTypeName(type, outputsBeingResolved, this::maybeResolveOutput);
		}

		private Optional<String> getTypeName(
			TypeRef type,
			Set<TypeRef> beingResolved,
			Function<TypeRef, ResolvedGraphQLType<?>> resolver)
		{
			TypeRef withoutUsage = type.withoutUsage();

			Optional<String> current = names.getName(withoutUsage);
			if(current.isPresent())
			{
				return current;
			}

			if(beingResolved.contains(withoutUsage))
			{
				// Already attempting to resolve this, can't resolve it again
				return Optional.empty();
			}

			ResolvedGraphQLType<?> gql = resolver.apply(type);
			if(! gql.isPresent())
			{
				return Optional.empty();
			}

			GraphQLType gqlType = gql.getGraphQLType();
			if(gqlType instanceof GraphQLList)
			{
				return Optional.of(((GraphQLList) gqlType).getWrappedType().getName() + "List");
			}
			else if(gqlType instanceof GraphQLModifiedType)
			{
				return Optional.of(((GraphQLModifiedType) gqlType).getWrappedType().getName());
			}
			else
			{
				return Optional.of(gqlType.getName());
			}
		}

		@Override
		public String requestInputTypeName(TypeRef type)
		{
			return requestTypeName(type, true, false);
		}

		@Override
		public String requestOutputTypeName(TypeRef type)
		{
			return requestTypeName(type, false, true);
		}

		@Override
		public String requestInputOutputTypeName(TypeRef type)
		{
			return requestTypeName(type, true, true);
		}

		private String requestTypeName(TypeRef type, boolean input, boolean output)
		{
			Optional<String> current = names.getName(type);
			if(current.isPresent())
			{
				return current.get();
			}

			try
			{
				String name = defaultNaming.compute(new GraphQLNamingEncounter()
				{
					@Override
					public GraphQLResolverContext getContext()
					{
						return ResolverContextImpl.this;
					}

					@Override
					public TypeRef getType()
					{
						return type;
					}

					@Override
					public boolean isInput()
					{
						return input;
					}

					@Override
					public boolean isOutput()
					{
						return output;
					}
				});

				names.reserveName(name, breadcrumb, type);

				return name;
			}
			catch(Exception e)
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
			return annotated.getAnnotation(GraphQLDescription.class)
				.map(GraphQLDescription::value)
				.orElse("");
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void registerResolved(
			TypeRef type,
			ResolvedGraphQLType<?> resolved,
			boolean isInput
		)
		{
			GraphQLType gql = resolved.getGraphQLType();
			if(gql instanceof GraphQLModifiedType)
			{
				gql = ((GraphQLModifiedType) gql).getWrappedType();
			}

			if(gql instanceof GraphQLInputType
				&& isInput || (! resolved.hasConversion() && ! resolved.hasDefaultValue())
			)
			{
				/*
				 * If the GraphQL type resolved is an input and we're either:
				 *
				 * 1) Resolving an input type
				 * 2) The resolved type has no extras like a conversion and default value
				 */
				builtInputTypes.put(type, (ResolvedGraphQLType) resolved);
			}

			if(gql instanceof GraphQLOutputType
				&& ! isInput || (! resolved.hasConversion() && ! resolved.hasDefaultValue())
			)
			{
				/*
				 * If the GraphQL type resolved is an output and we're either:
				 *
				 * 1) Resolving an output type
				 * 2) The resolved type has no extras like a conversion and default value
				 */
				builtOutputTypes.put(type, (ResolvedGraphQLType) resolved);
			}

			// Register the name
			names.reserveNameAllowDuplicate(type, gql.getName());
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLOutputType> maybeResolveOutput(TypeRef type)
		{
			TypeRef withoutUsage = type.withoutUsage();

			ResolvedGraphQLType<? extends GraphQLOutputType> graphQLType = ResolvedGraphQLType.none();
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

					String name = getOutputTypeName(withoutUsage)
						.orElseThrow(() ->  newError(
							"Could not resolve name for type " + type.toTypeName()
							+ ", during recursive resolution. Try reserving a "
							+ "name the name earlier in your resolver"
						));

					PendingDataFetchingConversion<?, ?> pending = pendingOutputConversions
						.computeIfAbsent(withoutUsage, (key) -> new PendingDataFetchingConversion<>());

					return ResolvedGraphQLType.forType(GraphQLTypeReference.typeRef(name))
						.withOutputConversion(pending);
				}

				for(GraphQLOutputResolver resolver : typeResolvers.getOutputResolver(type))
				{
					try
					{
						if(! (resolver instanceof ConvertingTypeResolver))
						{
							/*
							 * Only keep track of non-converting resolutions.
							 *
							 * This helps work around a case with recursive
							 * resolutions failing because the type being
							 * converted from does not have a name yet but its
							 * actual type will have.
							 */
							outputsBeingResolved.add(withoutUsage);
						}

						graphQLType = breadcrumb(Breadcrumb.forResolver(type, resolver), () -> {
							return resolver.resolveOutput(new OutputEncounterImpl(
								this,
								withoutUsage
							));
						});

						if(graphQLType.isPresent())
						{
							// Found a type, other resolvers will not run
							break;
						}
					}
					finally
					{
						outputsBeingResolved.remove(withoutUsage);
					}
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

				registerResolved(withoutUsage, graphQLType, false);
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

			throw newError(
				"Requested output binding of `" + type.toTypeName()
				+ "` but type is not resolvable to a GraphQL type. "
				+ "Types need to be annotated or bound via resolvers"
			);
		}

		@Override
		public ResolvedGraphQLType<? extends GraphQLInputType> maybeResolveInput(TypeRef type)
		{
			TypeRef withoutUsage = type.withoutUsage();

			ResolvedGraphQLType<? extends GraphQLInputType> graphQLType = ResolvedGraphQLType.none();
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

					String name = getOutputTypeName(withoutUsage)
						.orElseThrow(() ->  newError(
							"Could not resolve name for type " + type.toTypeName()
							+ ", during recursive resolution. Try reserving a "
							+ "name the name earlier in your resolver"
						));

					PendingDataFetchingConversion<?, ?> pending = pendingInputConversions
						.computeIfAbsent(withoutUsage, (key) -> new PendingDataFetchingConversion<>());

					return ResolvedGraphQLType.forType(GraphQLTypeReference.typeRef(name))
						.withInputConversion(pending);
				}

				for(GraphQLInputResolver resolver : typeResolvers.getInputResolver(type))
				{
					try
					{
						inputsBeingResolved.add(withoutUsage);

						graphQLType = breadcrumb(Breadcrumb.forType(type), () -> {
							return resolver.resolveInput(new InputEncounterImpl(
								this,
								withoutUsage
							));
						});

						if(graphQLType.isPresent())
						{
							// Found a type, other resolvers will not run
							break;
						}
					}
					finally
					{
						inputsBeingResolved.remove(withoutUsage);
					}
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

				registerResolved(withoutUsage, graphQLType, true);
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

			throw newError(
				"Requested input binding of `" + type.toTypeName()
				+ "` but type is not resolvable to a GraphQL type. "
				+ "Types need to be annotated or bound via resolvers"
			);
		}

		@Override
		public Set<TypeRef> findExtendingTypes(TypeRef type)
		{
			Set<TypeRef> result = new HashSet<>();
			for(Class<?> c : types)
			{
				if(type.getErasedType().isAssignableFrom(c) && c != type.getErasedType())
				{
					result.add(Types.reference(c));
				}
			}
			return result;
		}

		@Override
		public <T extends Annotation> Optional<T> findMetaAnnotation(Annotated annotated, Class<T> annotation)
		{
			return findMetaAnnotation(annotated.getAnnotations(), annotation);
		}

		@SuppressWarnings({ "unchecked" })
		private <T extends Annotation> Optional<T> findMetaAnnotation(Annotation[] annotations, Class<T> annotation)
		{
			for(Annotation a : annotations)
			{
				if(a.annotationType() == annotation)
				{
					// This is the annotation we're looking for
					return Optional.of((T) a);
				}

				// Skip looking on Java annotations - solves recursion for @Documented
				if(a.annotationType().getName().startsWith("java.")) continue;

				Map<Class<?>, Annotation> cached = annotationCache.computeIfAbsent(
					a.annotationType(),
					k -> new HashMap<>()
				);

				if(cached.containsKey(annotation))
				{
					// The annotation has been looked for and the result cached
					return Optional.ofNullable((T) cached.get(annotation));
				}

				// Look one step deeper for the annotation
				Optional<T> result = findMetaAnnotation(
					a.annotationType().getAnnotations(),
					annotation
				);
				cached.put(annotation, result.orElse(null));

				if(result.isPresent())
				{
					return result;
				}
			}

			return Optional.empty();
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public GraphQLDirectiveFieldResult applyFieldDirectives(
			Annotation[] annotations,
			GraphQLFieldDefinition field,
			DataFetchingSupplier<?> supplier
		)
		{
			for(Annotation a : annotations)
			{
				GraphQLDirectiveResolver<?> resolver = directives.get(a.annotationType());
				if(resolver == null) continue;

				if(! (resolver instanceof GraphQLDirectiveFieldResolver))
				{
					throw newError("The annotation " + a.annotationType().getSimpleName() + " was used on a GraphQL field, but directive resolver does not support fields");
				}

				GraphQLDirectiveFieldResolver fieldResolver = (GraphQLDirectiveFieldResolver) resolver;

				GraphQLDirective directive = builtDirectives.get(a.annotationType());
				if(directive == null)
				{
					directive = resolver.createDirective(new GraphQLDirectiveCreationEncounterImpl(this));
					builtDirectives.put(a.annotationType(), directive);
					schemaBuilder.additionalDirective(directive);
				}

				GraphQLDirectiveFieldEncounterImpl encounter = new GraphQLDirectiveFieldEncounterImpl(
					this,
					directive,
					a,
					field,
					supplier
				);

				fieldResolver.applyField(encounter);

				field = encounter.finalizeField();
				supplier = encounter.getSupplier();
			}

			return new GraphQLDirectiveFieldResult(
				field,
				supplier
			);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Optional<DataFetchingSupplier<?>> resolveSupplier(Annotation[] annotations, TypeRef type)
		{
			for(Map.Entry<Class<? extends Annotation>, GraphQLParameterResolver<?>> e : parameterResolvers.entrySet())
			{
				Optional<? extends Annotation> a = findMetaAnnotation(annotations, e.getKey());
				if(! a.isPresent()) continue;

				GraphQLParameterResolver resolver = e.getValue();
				DataFetchingSupplier<?> supplier = resolver.resolveParameter(new GraphQLParameterEncounter<Annotation>() {
					@Override
					public Annotation getAnnotation()
					{
						return a.get();
					}

					@Override
					public GraphQLResolverContext getContext()
					{
						return ResolverContextImpl.this;
					}

					@Override
					public TypeRef getType()
					{
						return type;
					}
				});

				if(supplier == null)
				{
					throw newError("Could not resolve how to handle " + a + " " + type.toTypeName());
				}

				return Optional.of(supplier);
			}

			return Optional.empty();
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
				objectMixins,
				context,
				context.codeRegistryBuilder
			);
		}

		@Override
		public GraphQLInterfaceBuilder newInterfaceType()
		{
			return new GraphQLInterfaceBuilderImpl(
				objectMixins,
				context
			);
		}

		@Override
		public GraphQLUnionBuilder newUnionType()
		{
			return new GraphQLUnionBuilderImpl(context);
		}
	}

	private static class InputEncounterImpl
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
