package se.l4.graphql.binding.internal.resolvers;

import java.util.Optional;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import se.l4.commons.types.Types;
import se.l4.commons.types.conversion.Conversion;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLScalar;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.ResolverContext;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

public class ScalarResolver
	implements GraphQLOutputResolver, GraphQLInputResolver
{
	private final GraphQLScalar<?, ?> scalar;

	public ScalarResolver(GraphQLScalar<?, ?> scalar)
	{
		this.scalar = scalar;
	}

	@Override
	public Optional<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		return Optional.of(resolve(encounter, encounter.getType()));
	}

	@Override
	public Optional<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		return Optional.of(resolve(encounter, encounter.getType()));
	}

	private GraphQLScalarType resolve(ResolverContext ctx, TypeRef type)
	{
		return ctx.breadcrumb(Breadcrumb.forType(type), () -> {
			TypeRef scalarType = Types.reference(scalar.getClass());
			String name;
			Optional<GraphQLName> nameAnnotation = scalarType.findAnnotation(GraphQLName.class);
			if(nameAnnotation.isPresent())
			{
				name = nameAnnotation.get().value();
				ctx.requestTypeName(name);
			}
			else
			{
				name = ctx.getTypeName(type);
			}

			TypeRef scalarInterface = scalarType.getInterface(GraphQLScalar.class).get();
			TypeRef graphQLType = scalarInterface.getTypeParameter(1).get();

			// Get a conversion that can convert from any object into the type requested
			Conversion inputConversion = ctx.getTypeConverter()
				.getDynamicConversion(Object.class, graphQLType.getErasedType());

			return GraphQLScalarType.newScalar()
				.name(name)
				.description(ctx.getDescription(scalarType))
				.coercing(new ScalarCoercionWrapper<>((GraphQLScalar) scalar, inputConversion))
				.build();
		});
	}

	public static class ScalarCoercionWrapper<JavaType, GraphQLType>
		implements Coercing<JavaType, GraphQLType>
	{
		private final GraphQLScalar<JavaType, GraphQLType> scalar;
		private final Conversion<Object, GraphQLType> inputConversion;

		public ScalarCoercionWrapper(
			GraphQLScalar<JavaType, GraphQLType> scalar,
			Conversion<Object, GraphQLType> inputConversion
		)
		{
			this.scalar = scalar;
			this.inputConversion = inputConversion;
		}

		@Override
		public GraphQLType serialize(Object dataFetcherResult)
			throws CoercingSerializeException
		{
			try
			{
				return scalar.serialize((JavaType) dataFetcherResult);
			}
			catch(Exception e)
			{
				throw new CoercingSerializeException("Could not serialize scalar; " + e.getMessage(), e);
			}
		}

		@Override
		public JavaType parseValue(Object input)
			throws CoercingParseValueException
		{
			try
			{
				GraphQLType converted = inputConversion.convert(input);
				return scalar.parseValue(converted);
			}
			catch(Exception e)
			{
				throw new CoercingParseValueException("Could not parse scalar; " + e.getMessage(), e);
			}
		}

		@Override
		public JavaType parseLiteral(Object input)
			throws CoercingParseLiteralException
		{
			try
			{
				GraphQLType converted = inputConversion.convert(input);
				return scalar.parseValue(converted);
			}
			catch(Exception e)
			{
				throw new CoercingParseLiteralException("Could not parse scalar; " + e.getMessage(), e);
			}
		}
	}
}