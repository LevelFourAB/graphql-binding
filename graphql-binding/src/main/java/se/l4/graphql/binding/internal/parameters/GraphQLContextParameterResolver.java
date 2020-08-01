package se.l4.graphql.binding.internal.parameters;

import java.util.Optional;

import se.l4.graphql.binding.ContextValue;
import se.l4.graphql.binding.annotations.GraphQLContext;
import se.l4.graphql.binding.internal.ContextValueImpl;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLParameterEncounter;
import se.l4.graphql.binding.resolver.GraphQLParameterResolver;
import se.l4.ylem.types.reflect.TypeRef;

/**
 * Resolver for parameters annotated with {@link GraphQLContext}.
 */
public class GraphQLContextParameterResolver
	implements GraphQLParameterResolver<GraphQLContext>
{

	@Override
	public DataFetchingSupplier<?> resolveParameter(GraphQLParameterEncounter<GraphQLContext> encounter)
	{
		TypeRef type = encounter.getType();
		GraphQLContext annotation = encounter.getAnnotation();

		String name = annotation.value();
		if(type.getErasedType() == Optional.class)
		{
			return env -> {
				graphql.GraphQLContext ctx = env.getContext();
				return Optional.ofNullable(ctx.get(name));
			};
		}
		else if(type.getErasedType() == ContextValue.class)
		{
			return env -> {
				graphql.GraphQLContext ctx = env.getContext();
				return new ContextValueImpl<>(ctx, name);
			};
		}
		else
		{
			return env -> {
				graphql.GraphQLContext ctx = env.getContext();
				return ctx.get(name);
			};
		}
	}
}
