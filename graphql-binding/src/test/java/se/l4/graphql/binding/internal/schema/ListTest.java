package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLType;
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
			return ImmutableList.of("one", "two");
		}

		@GraphQLField
		public List<Sub> outputSub()
		{
			return ImmutableList.of(
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

	@GraphQLType
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
