package se.l4.graphql.binding.internal.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
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
import se.l4.graphql.binding.annotations.GraphQLDescription;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.TypedGraphQLOutputResolver;

/**
 * Custom resolver that is registered whenever a scalar that uses
 * {@link GraphQLScalar} is added.
 */
public class ScalarResolver
	implements TypedGraphQLOutputResolver, TypedGraphQLInputResolver
{
	private final Class<?> type;
	private final GraphQLScalar<?, ?> scalar;

	public ScalarResolver(Class<?> type, GraphQLScalar<?, ?> scalar)
	{
		this.type = type;
		this.scalar = scalar;
	}

	@Override
	public Class<?> getType()
	{
		return type;
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(GraphQLOutputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(resolve(encounter.getContext(), encounter.getType()));
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		return ResolvedGraphQLType.forType(resolve(encounter.getContext(), encounter.getType()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private GraphQLScalarType resolve(GraphQLResolverContext ctx, TypeRef type)
	{
		return ctx.breadcrumb(Breadcrumb.forType(type), () -> {
			TypeRef scalarType = Types.reference(scalar.getClass());

			/*
			 * Resolve the name by first looking for an annotation of the
			 * GraphQLScalar implementation and then resolving against the
			 * Java type of the scalar.
			 */
			String name;
			Optional<GraphQLName> nameAnnotation = scalarType.getAnnotation(GraphQLName.class);
			if(nameAnnotation.isPresent())
			{
				name = nameAnnotation.get().value();
				ctx.requestTypeName(name);
			}
			else
			{
				name = ctx.getTypeName(type);
			}

			String description;
			Optional<GraphQLDescription> descriptionAnnotation = scalarType.getAnnotation(GraphQLDescription.class);
			if(descriptionAnnotation.isPresent())
			{
				description = descriptionAnnotation.get().value();
			}
			else
			{
				description = ctx.getDescription(type);
			}

			// Resolve the interface and the GraphQL type and request a conversion to iit
			TypeRef scalarInterface = scalarType.getInterface(GraphQLScalar.class).get();
			TypeRef graphQLType = scalarInterface.getTypeParameter(1).get();

			// Get a conversion that can convert from any object into the type requested
			Conversion inputConversion = ctx.getTypeConverter()
				.getDynamicConversion(Object.class, graphQLType.getErasedType());

			// Create the GraphQL type representing the scalar
			return GraphQLScalarType.newScalar()
				.name(name)
				.description(description)
				.coercing(new CustomCoercing<>((GraphQLScalar) scalar, inputConversion))
				.build();
		});
	}

	/**
	 * Implementation of {@link Coercing} that delegates work to an instance
	 * of {@link GraphQLScalar}.
	 *
	 * @param <JavaType>
	 * @param <GraphQLType>
	 */
	public static class CustomCoercing<JavaType, GraphQLType>
		implements Coercing<JavaType, GraphQLType>
	{
		private final GraphQLScalar<JavaType, GraphQLType> scalar;
		private final Conversion<Object, GraphQLType> inputConversion;

		public CustomCoercing(
			GraphQLScalar<JavaType, GraphQLType> scalar,
			Conversion<Object, GraphQLType> inputConversion
		)
		{
			this.scalar = scalar;
			this.inputConversion = inputConversion;
		}

		@Override
		@SuppressWarnings("unchecked")
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
			return parseLiteral(input, Collections.emptyMap());
		}

		@Override
		public JavaType parseLiteral(Object input, Map<String, Object> variables)
			throws CoercingParseLiteralException
		{
			try
			{
				Object literalInput = convertLiteral(input, variables);
				GraphQLType converted = inputConversion.convert(literalInput);
				return scalar.parseValue(converted);
			}
			catch(Exception e)
			{
				throw new CoercingParseLiteralException("Could not parse scalar; " + e.getMessage(), e);
			}
		}

		public Object convertLiteral(Object input, Map<String, Object> variables)
			throws CoercingParseLiteralException
		{
			if(input instanceof NullValue)
			{
				return null;
			}
			else if(input instanceof FloatValue)
			{
				return ((FloatValue) input).getValue();
			}
			else if(input instanceof StringValue)
			{
				return ((StringValue) input).getValue();
			}
			else if (input instanceof IntValue)
			{
				return ((IntValue) input).getValue();
			}
			else if (input instanceof BooleanValue)
			{
				return ((BooleanValue) input).isValue();
			}
			else if (input instanceof EnumValue)
			{
				return ((EnumValue) input).getName();
			}
			else if(input instanceof VariableReference)
			{
				String varName = ((VariableReference) input).getName();
				return variables.get(varName);
			}
			else if(input instanceof ArrayValue)
			{
				@SuppressWarnings("rawtypes")
				List<Value> values = ((ArrayValue) input).getValues();
				return values.stream()
					.map(v -> convertLiteral(v, variables))
					.collect(ImmutableList.toImmutableList());
			}
			else if(input instanceof ObjectValue)
			{
				List<ObjectField> values = ((ObjectValue) input).getObjectFields();
				ImmutableMap.Builder<String, Object> parsedValues = ImmutableMap.builder();
				values.forEach(field -> {
					Object parsedValue = convertLiteral(field.getValue(), variables);
					parsedValues.put(field.getName(), parsedValue);
				});
				return parsedValues.build();
			}

			throw new CoercingParseLiteralException();
		}
	}
}
