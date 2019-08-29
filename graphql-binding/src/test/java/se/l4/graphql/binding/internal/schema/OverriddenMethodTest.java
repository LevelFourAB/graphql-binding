package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.GraphQLTest;

public class OverriddenMethodTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testExecute()
	{
		Result result = execute("{ hello }");
		result.assertNoErrors();

		assertThat(result.pick("hello"), is("hello"));
	}

	public class Root
		extends Parent
	{
		@Override
		public String hello()
		{
			return "hello";
		}
	}

	public abstract class Parent
	{
		@GraphQLField
		public abstract String hello();
	}
}
