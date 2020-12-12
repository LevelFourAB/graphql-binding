package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import graphql.Scalars;
import graphql.schema.GraphQLList;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.internal.GraphQLMatchers;
import se.l4.graphql.binding.internal.GraphQLTest;

public class ListTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testSchema()
	{
		graphql.schema.GraphQLType type = schema.getQueryType().getFieldDefinition("outputString")
			.getType();

		assertThat("outputString returns list of strings", type, GraphQLMatchers.isSameType(GraphQLList.list(Scalars.GraphQLString)));

		type = schema.getQueryType().getFieldDefinition("outputStringNonNull")
			.getType();

		assertThat("outputStringNonNull returns non-null list of strings", type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(GraphQLList.list(Scalars.GraphQLString))));

		type = schema.getQueryType().getFieldDefinition("outputStringNonNullItems")
			.getType();

		assertThat("outputStringNonNullItems returns list of non-null strings", type, GraphQLMatchers.isSameType(GraphQLList.list(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString))));
	}


	@Test
	public void testOutputWithScalar()
	{
		Result result = execute("{ outputString }");
		result.assertNoErrors();

		assertThat(result.pick("outputString", "0"), is("one"));
		assertThat(result.pick("outputString", "1"), is("two"));
	}

	@Test
	public void testOutputWithSub()
	{
		Result result = execute("{ outputSub { id } }");
		result.assertNoErrors();

		assertThat(result.pick("outputSub", "0", "id"), is("one"));
		assertThat(result.pick("outputSub", "1", "id"), is("two"));
	}

	@Test
	public void testInputLiteral()
	{
		Result result = execute("{ input(strings: [ \"1\", \"2\" ]) }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is(2));
	}

	public class Root
	{
		@GraphQLField
		public List<String> outputString()
		{
			return List.of("one", "two");
		}

		@GraphQLField
		@GraphQLNonNull
		public List<String> outputStringNonNull()
		{
			return List.of("one", "two");
		}

		@GraphQLField
		public List<@GraphQLNonNull String> outputStringNonNullItems()
		{
			return List.of("one", "two");
		}

		@GraphQLField
		public List<Sub> outputSub()
		{
			return List.of(
				new Sub("one"),
				new Sub("two")
			);
		}

		@GraphQLField
		public int input(
			@GraphQLNonNull @GraphQLName("strings") List<String> strings
		)
		{
			return strings.size();
		}
	}

	@GraphQLObject
	public class Sub
	{
		@GraphQLField
		public final String id;

		public Sub(String id)
		{
			this.id = id;
		}
	}
}
