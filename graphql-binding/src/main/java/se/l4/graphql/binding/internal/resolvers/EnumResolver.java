package se.l4.graphql.binding.internal.resolvers;

import java.util.Optional;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.annotations.GraphQLDeprecated;
import se.l4.graphql.binding.annotations.GraphQLDescription;
import se.l4.graphql.binding.annotations.GraphQLEnum;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.ylem.types.reflect.FieldRef;
import se.l4.ylem.types.reflect.TypeRef;

public class EnumResolver
	implements GraphQLOutputResolver, GraphQLInputResolver
{
	@Override
	public boolean supportsInput(TypeRef type)
	{
		return type.hasAnnotation(GraphQLEnum.class);
	}

	@Override
	public boolean supportsOutput(TypeRef type)
	{
		return supportsInput(type);
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(
		GraphQLOutputEncounter encounter
	)
	{
		return resolve(encounter.getContext(), encounter.getType());
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(
		GraphQLInputEncounter encounter
	)
	{
		return resolve(encounter.getContext(), encounter.getType());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ResolvedGraphQLType<GraphQLEnumType> resolve(
		GraphQLResolverContext context,
		TypeRef type
	)
	{
		GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
			.name(context.requestInputOutputTypeName(type))
			.description(context.getDescription(type));

		Class<? extends Enum> enumType = (Class<? extends Enum>) type.getErasedType();
		for(Enum<?> constant : enumType.getEnumConstants())
		{
			Optional<FieldRef> field = type.getField(constant.name());

			String name = field
				.flatMap(t -> t.getAnnotation(GraphQLName.class))
				.map(a -> a.value())
				.orElse(constant.name());

			String description = field
				.flatMap(t -> t.getAnnotation(GraphQLDescription.class))
				.map(a -> a.value())
				.orElse(null);

			String deprecationReason = field
				.flatMap(t -> t.getAnnotation(GraphQLDeprecated.class))
				.map(a -> a.value())
				.orElse(null);

			builder.value(GraphQLEnumValueDefinition.newEnumValueDefinition()
				.name(name)
				.value(constant)
				.description(description)
				.deprecationReason(deprecationReason)
				.build()
			);
		}

		return ResolvedGraphQLType.forType(builder.build());
	}

	@Override
	public String toString()
	{
		return "@" + GraphQLEnum.class.getSimpleName();
	}

}
