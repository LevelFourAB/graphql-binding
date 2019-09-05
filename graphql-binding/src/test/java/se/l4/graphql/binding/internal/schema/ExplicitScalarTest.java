package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.internal.GraphQLTest;
import se.l4.graphql.binding.resolver.GraphQLScalarResolver;

public class ExplicitScalarTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder
			.withRoot(new Root())
			.withResolver(new GraphQLScalarResolver<TestScalar, String>() {
				@Override
				public TestScalar parseValue(String input)
				{
					return new TestScalar(input);
				}

				@Override
				public String serialize(TestScalar instance)
				{
					return instance.value;
				}
			});
	}

	@Test
	public void testOutput()
	{
		Result result = execute("{ output }");
		result.assertNoErrors();

		assertThat(result.pick("output"), is("value"));
	}

	@Test
	public void testInputLiteral()
	{
		Result result = execute("{ input(in: \"test\") }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is("test"));
	}

	@Test
	public void testInputVariable()
	{
		Result result = execute("query($in: TestScalar) { input(in: $in) }", ImmutableMap.of(
			"in", "test"
		));
		result.assertNoErrors();

		assertThat(result.pick("input"), is("test"));
	}

	public class TestScalar
	{
		private String value;

		public TestScalar(String value)
		{
			this.value = value;
		}
	}

	public class Root
	{

		@GraphQLField
		public TestScalar output()
		{
			return new TestScalar("value");
		}

		@GraphQLField
		public String input(
			@GraphQLName("in") TestScalar in
		)
		{
			return in.value;
		}

	}
}
