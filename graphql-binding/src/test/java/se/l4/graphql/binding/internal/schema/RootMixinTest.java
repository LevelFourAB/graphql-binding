package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLMixinField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class RootMixinTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testName()
	{
		Result result = execute("{ output { name } }");
		result.assertNoErrors();

		assertThat(result.pick("output", "name"), is("1234"));
	}

	@Test
	public void testNameIs()
	{
		Result result = execute("{ output { nameIs(value: \"1234\") } }");
		result.assertNoErrors();

		assertThat(result.pick("output", "nameIs"), is(true));
	}

	public class Root
	{
		@GraphQLField
		public TestObject output()
		{
			return new TestObject("1234");
		}

		@GraphQLMixinField
		public boolean nameIs(
			@GraphQLSource TestObject object,
			@GraphQLName("value") String value
		)
		{
			return Objects.equals(object.name, value);
		}

		@GraphQLMixinField
		public boolean nameIs(
			@GraphQLSource String object,
			@GraphQLName("value") String value
		)
		{
			return false;
		}
	}

	@GraphQLObject
	public class TestObject
	{
		@GraphQLField
		public final String name;

		public TestObject(String name)
		{
			this.name = name;
		}
	}
}
