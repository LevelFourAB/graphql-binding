package se.l4.graphql.binding.internal;

import org.junit.Test;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLType;

public class GraphQLBuilderTest
{
	@Test
	public void testRoot()
	{
		InternalGraphQLSchemaBuilder builder = new InternalGraphQLSchemaBuilder();
		builder.addOutputType(SimpleType.class);
		builder.addRootType(Root.class, Root::new);

		GraphQLSchema schema = builder.build();
		schema.getType("SimpleType");

		GraphQL ql = GraphQL.newGraphQL(schema)
			.build();

		ExecutionResult r = ql.execute("query { type { test, doStuff(input: 10) } }");
		System.out.println(r.getErrors());
		System.out.println((Object) r.getData());
	}

	public static class Root
	{
		@GraphQLField
		public final SimpleType type = new SimpleType();
	}

	@GraphQLType
	public static class SimpleType
	{
		@GraphQLField
		public int test = 10;

		@GraphQLField
		public String doStuff(
			@GraphQLName("input") int input
		)
		{
			return "stuff " + input;
		}
	}
}
