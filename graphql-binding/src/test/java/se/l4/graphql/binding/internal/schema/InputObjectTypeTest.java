package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.internal.GraphQLTest;

public class InputObjectTypeTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testLiteral()
	{
		Result result = execute(" { doStuff(in: { name: \"test\" }) }");
		result.assertNoErrors();

		assertThat(result.pick("doStuff"), is("test"));
	}

	@Test
	public void testVariable()
	{
		Result result = execute("query($in: TestInput) { doStuff(in: $in) }", Map.of("in",
			Map.of("name", "test")
		));
		result.assertNoErrors();

		assertThat(result.pick("doStuff"), is("test"));
	}

	@Test
	public void testSubLiteral()
	{
		Result result = execute(" { doStuff(in: { name: \"test\", sub: { active: true } }) }");
		result.assertNoErrors();

		assertThat(result.pick("doStuff"), is("TEST"));
	}

	@Test
	public void testSubVariable()
	{
		Result result = execute("query($in: TestInput) { doStuff(in: $in) }", Map.of("in",
			Map.of("name", "test", "sub", Map.of("active", "true"))
		));
		result.assertNoErrors();

		assertThat(result.pick("doStuff"), is("TEST"));
	}

	public class Root
	{
		@GraphQLField
		public String doStuff(
			@GraphQLName("in") TestInput in
		)
		{
			if(in.sub != null && in.sub.active)
			{
				return in.name.toUpperCase();
			}

			return in.name;
		}
	}

	@GraphQLInputObject
	public static class TestInput
	{
		@GraphQLField
		public String name;

		@GraphQLField
		public SubTestInput sub;
	}

	@GraphQLInputObject
	public static class SubTestInput
	{
		@GraphQLField
		public boolean active;
	}
}
