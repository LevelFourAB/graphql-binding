package se.l4.graphql.binding.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.builders.GraphQLObjectBuilderImpl;
import se.l4.graphql.binding.internal.resolvers.ListResolver;
import se.l4.graphql.binding.internal.resolvers.ScalarResolver;
import se.l4.graphql.binding.internal.resolvers.TypeResolver;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.ResolverContext;
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
			TypeResolver.resolve(
				ctx,
				Types.reference(e.getKey()),
				env -> supplier.get(),
				builder
			);
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

		return builder.codeRegistry(codeRegistryBuilder.build())
			.build();
	}

	private class ResolverContextImpl
		implements ResolverContext
	{
		private final GraphQLSchema.Builder builder;

		protected final GraphQLCodeRegistry.Builder codeRegistryBuilder;

		private Breadcrumb breadcrumb;

		public ResolverContextImpl(
			GraphQLSchema.Builder builder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder
		)
		{
			this.builder = builder;
			this.codeRegistryBuilder = codeRegistryBuilder;

			breadcrumb = Breadcrumb.empty();
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
				Optional<GraphQLOutputResolver> resolver = typeResolvers.getOutputResolver(type.getErasedType());
				if(! resolver.isPresent())
				{
					return ResolvedGraphQLType.none();
				}

				graphQLType = resolver.get().resolveOutput(new OutputEncounterImpl(
					builder,
					codeRegistryBuilder,

					withoutUsage
				));

				if(! graphQLType.isPresent())
				{
					return ResolvedGraphQLType.none();
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
				Optional<GraphQLInputResolver> resolver = typeResolvers.getInputResolver(type.getErasedType());
				if(! resolver.isPresent())
				{
					return ResolvedGraphQLType.none();
				}

				graphQLType = resolver.get().resolveInput(new InputEncounterImpl(
					builder,
					codeRegistryBuilder,

					withoutUsage
				));

				if(! graphQLType.isPresent())
				{
					return ResolvedGraphQLType.none();
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
		extends ResolverContextImpl
		implements GraphQLOutputEncounter
	{
		private final TypeRef type;

		public OutputEncounterImpl(
			GraphQLSchema.Builder builder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder,

			TypeRef type
		)
		{
			super(builder, codeRegistryBuilder);

			this.type = type;
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
				this,
				codeRegistryBuilder
			);
		}
	}

	private class InputEncounterImpl
		extends ResolverContextImpl
		implements GraphQLInputEncounter
	{
		private final TypeRef type;

		public InputEncounterImpl(
			GraphQLSchema.Builder builder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder,

			TypeRef type
		)
		{
			super(builder, codeRegistryBuilder);

			this.type = type;
		}

		@Override
		public TypeRef getType()
		{
			return type;
		}

	}
}
