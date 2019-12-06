package se.l4.graphql.binding.internal;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;
import se.l4.graphql.binding.GraphQLBinder;

public abstract class GraphQLTest
{
	protected GraphQLSchema schema;
	protected GraphQL ql;

	@Before
	public void beforeTest()
	{
		GraphQLBinder binder = GraphQLBinder.newBinder();
		setup(binder);
		this.schema = binder.build();

		ql = GraphQL.newGraphQL(schema)
			.build();
	}

	protected abstract void setup(GraphQLBinder binder);

	protected Result execute(String query)
	{
		ExecutionResult result = ql.execute(ExecutionInput.newExecutionInput(query)
			.context(GraphQLContext.newContext()
				.of("test", "TestEnv")
			)
			.build()
		);

		return new Result(result);
	}

	protected Result execute(String query, Map<String, Object> variables)
	{
		ExecutionResult result = ql.execute(ExecutionInput.newExecutionInput(query)
			.variables(variables)
			.context(GraphQLContext.newContext()
				.of("test", "TestEnv")
			)
			.build()
		);

		return new Result(result);
	}

	protected class Result
	{
		private ExecutionResult result;

		public Result(ExecutionResult result)
		{
			this.result = result;
		}

		public void assertNoErrors()
		{
			if(! result.getErrors().isEmpty())
			{
				AssertionError error = new AssertionError("Expected no errors, got: " + result.getErrors().stream()
					.map(e -> e.getMessage())
					.collect(Collectors.joining(", "))
				);

				for(GraphQLError e : result.getErrors())
				{
					if(e instanceof ExceptionWhileDataFetching)
					{
						error.addSuppressed(((ExceptionWhileDataFetching) e).getException());
					}
					else if(e instanceof Throwable)
					{
						error.addSuppressed((Throwable) e);
					}
				}

				throw error;
			}
		}

		public <T> T getData()
		{
			return result.getData();
		}

		public <T> T pick(String... path)
		{
			return (T) pick((Map) result.getData(), path, 0);
		}

		private Object pick(Map<String, Object> data, String[] path, int index)
		{
			String part = path[index];
			Object partData = data.get(part);
			if(partData == null)
			{
				return null;
			}

			if(index == path.length - 1)
			{
				return partData;
			}

			if(partData instanceof List)
			{
				return pick((List) partData, path, index + 1);
			}
			else if(partData instanceof Map)
			{
				return pick((Map) partData, path, index + 1);
			}
			else
			{
				throw new AssertionError("Can not traverse down into object at key " + part + ": " + partData);
			}
		}

		private Object pick(List data, String[] path, int index)
		{
			String part = path[index];
			int parsedPart = Integer.parseInt(part);

			if(parsedPart >= data.size()) return null;
			Object partData = data.get(parsedPart);

			if(index == path.length - 1)
			{
				return partData;
			}
			else if(partData instanceof List)
			{
				return pick((List) partData, path, index + 1);
			}
			else if(partData instanceof Map)
			{
				return pick((Map) partData, path, index + 1);
			}
			else
			{
				throw new AssertionError("Can not traverse down into object at key " + part + ": " + partData);
			}
		}
	}
}
