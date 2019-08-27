package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.GraphQLTest;

public class RootWithScalarTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testScalarField()
	{
		Result result = execute("{ id }");
		result.assertNoErrors();

		assertThat(result.pick("id"), is("test"));
	}

	@Test
	public void testScalarMethod()
	{
		Result result = execute("{ get }");
		result.assertNoErrors();

		assertThat(result.pick("get"), is("value"));
	}

	@Test
	public void testScalarMethodWithInput()
	{
		Result result = execute("{ do2(in: 2) }");
		result.assertNoErrors();

		assertThat(result.pick("do2"), is("v2"));
	}

	public class Root
	{
		@GraphQLField
		public final String id = "test";

		@GraphQLField
		public String get()
		{
			return "value";
		}

		@GraphQLField
		public String do2(
			@GraphQLNonNull @GraphQLName("in") int in
		)
		{
			return "v" + in;
		}
	}
}
