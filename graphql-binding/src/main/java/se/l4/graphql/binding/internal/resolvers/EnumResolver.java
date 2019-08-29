package se.l4.graphql.binding.internal.resolvers;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLType;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.graphql.binding.resolver.query.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.query.GraphQLOutputResolver;

public class EnumResolver
	implements GraphQLOutputResolver, GraphQLInputResolver
{

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

	private ResolvedGraphQLType<GraphQLEnumType> resolve(
		GraphQLResolverContext context,
		TypeRef type
	)
	{
		if(! type.hasAnnotation(GraphQLType.class))
		{
			return ResolvedGraphQLType.none();
		}

		GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
			.name(context.getTypeName(type))
			.description(context.getDescription(type));

		Class<? extends Enum> enumType = (Class<? extends Enum>) type.getErasedType();
		for(Enum<?> constant : enumType.getEnumConstants())
		{
			String name = type.getField(constant.name())
				.flatMap(t -> t.getAnnotation(GraphQLName.class))
				.map(a -> a.value())
				.orElse(constant.name());

			builder.value(
				name, constant
			);
		}

		return ResolvedGraphQLType.forType(builder.build());
	}
}
