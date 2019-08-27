package se.l4.graphql.binding.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import se.l4.commons.types.DefaultInstanceFactory;
import se.l4.commons.types.InstanceFactory;
import se.l4.commons.types.Types;
import se.l4.commons.types.reflect.Annotated;
import se.l4.commons.types.reflect.MemberRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.annotations.GraphQLDescription;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.builders.GraphQLObjectBuilderImpl;
import se.l4.graphql.binding.internal.resolvers.TypeResolver;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.ResolverContext;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLObjectBuilder;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;

/**
 * Internal class that does the actual resolving of types.
 */
public class InternalGraphQLSchemaBuilder
{
	private final NameRegistry names;
	private final TypeResolverRegistry typeResolvers;

	private final Map<Class<?>, Supplier<?>> rootTypes;
	private final List<Class<?>> types;

	private final Map<TypeRef, GraphQLOutputType> builtOutputTypes;
	private final Map<TypeRef, GraphQLInputType> builtInputTypes;

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

		registerBuiltin(Scalars.GraphQLString, String.class);
		registerBuiltin(Scalars.GraphQLChar, char.class, Character.class);

		registerBuiltin(Scalars.GraphQLBoolean, boolean.class, Boolean.class);

		registerBuiltin(Scalars.GraphQLByte, byte.class, Byte.class);
		registerBuiltin(Scalars.GraphQLShort, short.class, Short.class);
		registerBuiltin(Scalars.GraphQLInt, int.class, Integer.class);
		registerBuiltin(Scalars.GraphQLLong, long.class, Long.class);

		registerBuiltin(Scalars.GraphQLBigInteger, BigInteger.class);
		registerBuiltin(Scalars.GraphQLBigDecimal, BigDecimal.class);
	}

	/**
	 * Register a built in scalar and bind it to the specified types.
	 */
	private void registerBuiltin(GraphQLScalarType graphQLType, Class<?>... types)
	{
		for(Class<?> t : types)
		{
			TypeRef type = Types.reference(t);
			builtOutputTypes.put(type, graphQLType);
			builtInputTypes.put(type, graphQLType);
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
			instanceFactory,
			builder,
			codeRegistryBuilder
		);

		for(Class<?> type : types)
		{
			TypeRef typeRef = Types.reference(type);

			Optional<GraphQLOutputType> output = ctx.maybeResolveOutput(typeRef);
			if(output.isPresent())
			{
				builder.additionalType(output.get());
			}

			Optional<GraphQLInputType> input = ctx.maybeResolveInput(typeRef);
			if(input.isPresent())
			{
				builder.additionalType(input.get());
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
		private final InstanceFactory instanceFactory;
		private final GraphQLSchema.Builder builder;

		protected final GraphQLCodeRegistry.Builder codeRegistryBuilder;

		private Breadcrumb breadcrumb;

		public ResolverContextImpl(
			InstanceFactory instanceFactory,
			GraphQLSchema.Builder builder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder
		)
		{
			this.instanceFactory = instanceFactory;

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

		private void registerResolved(TypeRef type, GraphQLType graphQLType)
		{
			if(graphQLType instanceof GraphQLInputType)
			{
				builtInputTypes.put(type, (GraphQLInputType) graphQLType);
			}

			if(graphQLType instanceof GraphQLOutputType)
			{
				builtOutputTypes.put(type, (GraphQLOutputType) graphQLType);
			}
		}

		@Override
		public Optional<GraphQLOutputType> maybeResolveOutput(TypeRef type)
		{
			TypeRef withoutUsage = type.withoutUsage();

			GraphQLOutputType graphQLType;
			if(builtOutputTypes.containsKey(withoutUsage))
			{
				graphQLType = builtOutputTypes.get(withoutUsage);
			}
			else
			{
				Optional<? extends GraphQLOutputType> resolved = typeResolvers.getOutputResolver(type.getErasedType())
					.flatMap(resolver -> resolver.resolveOutput(new OutputEncounterImpl(
						builder,
						codeRegistryBuilder,

						withoutUsage
					)));

				if(! resolved.isPresent())
				{
					return Optional.empty();
				}

				graphQLType = resolved.get();
				registerResolved(withoutUsage, graphQLType);
			}

			if(type.getUsage().hasAnnotation(GraphQLNonNull.class))
			{
				graphQLType = graphql.schema.GraphQLNonNull.nonNull(graphQLType);
			}

			return Optional.of(graphQLType);
		}

		@Override
		public GraphQLOutputType resolveOutput(TypeRef type)
		{
			return maybeResolveOutput(type)
				.orElseThrow(() -> new GraphQLMappingException(
					"Requested output binding of `" + type.toTypeName()
					+ "` but type is not resolvable to a GraphQL type. "
					+ "Types need to be annotated or bound via resolvers"
				));
		}

		@Override
		public Optional<GraphQLInputType> maybeResolveInput(TypeRef type)
		{
			TypeRef withoutUsage = type.withoutUsage();

			GraphQLInputType graphQLType;
			if(builtInputTypes.containsKey(withoutUsage))
			{
				graphQLType = builtInputTypes.get(withoutUsage);
			}
			else
			{
				Optional<? extends GraphQLInputType> resolved = typeResolvers.getInputResolver(type.getErasedType())
					.flatMap(resolver -> resolver.resolveInput(new InputEncounterImpl(
						builder,
						codeRegistryBuilder,

						withoutUsage
					)));

				if(! resolved.isPresent())
				{
					return Optional.empty();
				}

				graphQLType = resolved.get();
				registerResolved(withoutUsage, graphQLType);
			}

			if(type.getUsage().hasAnnotation(GraphQLNonNull.class))
			{
				graphQLType = graphql.schema.GraphQLNonNull.nonNull(graphQLType);
			}

			return Optional.of(graphQLType);
		}

		@Override
		public GraphQLInputType resolveInput(TypeRef type)
		{
			return maybeResolveInput(type)
				.orElseThrow(() -> new GraphQLMappingException(
					"Requested input binding of `" + type.toTypeName()
					+ "` but type is not resolvable to a GraphQL type. "
					+ "Types need to be annotated or bound via resolvers"
				));
		}
	}

	private class OutputEncounterImpl
		extends ResolverContextImpl
		implements GraphQLOutputEncounter
	{
		private final TypeRef type;

		public OutputEncounterImpl(
			InstanceFactory instanceFactory,

			GraphQLSchema.Builder builder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder,

			TypeRef type
		)
		{
			super(instanceFactory, builder, codeRegistryBuilder);

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
			InstanceFactory instanceFactory,

			GraphQLSchema.Builder builder,
			GraphQLCodeRegistry.Builder codeRegistryBuilder,

			TypeRef type
		)
		{
			super(instanceFactory, builder, codeRegistryBuilder);

			this.type = type;
		}

		@Override
		public TypeRef getType()
		{
			return type;
		}

	}
}
