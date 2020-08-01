package se.l4.graphql.binding.internal.resolvers;

import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.annotations.GraphQLUnion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.output.GraphQLOutputEncounter;
import se.l4.graphql.binding.resolver.output.GraphQLOutputResolver;
import se.l4.graphql.binding.resolver.output.GraphQLUnionBuilder;
import se.l4.ylem.types.reflect.TypeRef;

public class UnionResolver
	implements GraphQLOutputResolver
{
	@Override
	public boolean supportsOutput(TypeRef type)
	{
		return type.hasAnnotation(GraphQLUnion.class);
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLOutputType> resolveOutput(
		GraphQLOutputEncounter encounter
	)
	{
		TypeRef initialType = encounter.getType();

		GraphQLResolverContext context = encounter.getContext();

		GraphQLUnionBuilder builder = encounter.newUnionType()
			.over(initialType);


		for(TypeRef type : context.findExtendingTypes(initialType))
		{
			builder.addPossibleType(type);
		}

		return ResolvedGraphQLType.forType(builder.build());
	}

	@Override
	public String toString()
	{
		return "@" + GraphQLUnion.class.getSimpleName();
	}

}
