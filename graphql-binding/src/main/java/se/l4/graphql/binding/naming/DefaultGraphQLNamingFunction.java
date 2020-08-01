package se.l4.graphql.binding.naming;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLType;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.ylem.types.reflect.TypeRef;

public class DefaultGraphQLNamingFunction
	implements GraphQLNamingFunction
{

	@Override
	public String compute(GraphQLNamingEncounter encounter)
	{
		GraphQLResolverContext context = encounter.getContext();
		TypeRef type = encounter.getType();

		// Find the name for this type without any generics
		String proposedName = type.getAnnotation(GraphQLName.class)
			.map(a -> a.value())
			.orElseGet(() -> type.getErasedType().getSimpleName());

		// Go through and add any type parameters to the name
		List<String> items = new LinkedList<>();
		items.add(proposedName);

		for(TypeRef typeParam : type.getTypeParameters())
		{
			ResolvedGraphQLType<? extends GraphQLType> graphQLType = encounter.isOutput()
				? context.maybeResolveOutput(typeParam)
				: context.maybeResolveInput(typeParam);

			if(graphQLType.isPresent())
			{
				GraphQLType gql = graphQLType.getGraphQLType();
				if(gql instanceof GraphQLList)
				{
					items.add(0, "List");
					gql = ((GraphQLList) gql).getWrappedType();
				}
				else if(gql instanceof GraphQLModifiedType)
				{
					gql = ((GraphQLModifiedType) gql).getWrappedType();
				}

				if(gql instanceof GraphQLNamedType)
				{
					items.add(0, ((GraphQLNamedType) gql).getName());
				}
			}
		}

		return items.stream().collect(Collectors.joining());
	}

}
