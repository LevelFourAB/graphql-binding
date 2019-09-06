package se.l4.graphql.binding.internal.parameters;

import graphql.cachecontrol.CacheControl;
import graphql.execution.directives.QueryDirectives;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.annotations.GraphQLEnvironment;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLParameterEncounter;
import se.l4.graphql.binding.resolver.GraphQLParameterResolver;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;

/**
 * Resolver for parameters annotated with {@link GraphQLEnvironment}.
 */
public class GraphQLEnvironmentParameterResolver
	implements GraphQLParameterResolver<GraphQLEnvironment>
{

	@Override
	public DataFetchingSupplier<?> resolveParameter(GraphQLParameterEncounter<GraphQLEnvironment> encounter)
	{
		GraphQLResolverContext context = encounter.getContext();
		TypeRef type = encounter.getType();

		Class<?> erasedType = type.getErasedType();
		if(erasedType == DataFetchingEnvironment.class)
		{
			return env -> env;
		}
		else if(erasedType == QueryDirectives.class)
		{
			return env -> env.getQueryDirectives();
		}
		else if(erasedType == DataFetchingFieldSelectionSet.class)
		{
			return env -> env.getSelectionSet();
		}
		else if(erasedType == CacheControl.class)
		{
			return env -> env.getCacheControl();
		}
		else
		{
			throw context.newError("@GraphQLEnvironment was used with unsupported type `" + type.toTypeName() + "`");
		}
	}

}
