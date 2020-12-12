package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.internal.GraphQLTest;

public class IntegerScalarTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testOutputInt()
	{
		Result result = execute("{ outputInt }");
		result.assertNoErrors();

		assertThat(result.pick("outputInt"), is(0));
	}

	@Test
	public void testInputIntPresent()
	{
		Result result = execute("{ inputInt(in: 2) }");
		result.assertNoErrors();

		assertThat(result.pick("inputInt"), is(2));
	}

	@Test
	public void testInputIntSkipped()
	{
		Result result = execute("{ inputInt }");
		result.assertNoErrors();

		assertThat(result.pick("inputInt"), is(0));
	}

	@Test
	public void testOutputInteger()
	{
		Result result = execute("{ outputInteger }");
		result.assertNoErrors();

		assertThat(result.pick("outputInteger"), is(0));
	}

	@Test
	public void testInputIntegerPresent()
	{
		Result result = execute("{ inputInteger(in: 2) }");
		result.assertNoErrors();

		assertThat(result.pick("inputInteger"), is(2));
	}

	@Test
	public void testInputIntegerSkipped()
	{
		Result result = execute("{ inputInteger }");
		result.assertNoErrors();

		assertThat(result.pick("inputInteger"), nullValue());
	}

	public class Root
	{
		@GraphQLField
		public int outputInt()
		{
			return 0;
		}

		@GraphQLField
		public int inputInt(
			@GraphQLName("in") int in
		)
		{
			return in;
		}

		@GraphQLField
		public Integer outputInteger()
		{
			return 0;
		}

		@GraphQLField
		public Integer inputInteger(
			@GraphQLName("in") Integer in
		)
		{
			return in;
		}
	}
}
