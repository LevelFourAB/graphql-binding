package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.ContextValue;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLContext;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.GraphQLTest;

public class ContextTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testDirect()
	{
		Result result = execute("{ direct }");
		result.assertNoErrors();

		assertThat(result.pick("direct"), is(true));
	}

	@Test
	public void testIndirect()
	{
		Result result = execute("{ indirect }");
		result.assertNoErrors();

		assertThat(result.pick("indirect"), is(true));
	}

	@Test
	public void testOptional()
	{
		Result result = execute("{ optional }");
		result.assertNoErrors();

		assertThat(result.pick("optional"), is(true));
	}

	@Test
	public void testContextValue()
	{
		Result result = execute("{ contextValue }");
		result.assertNoErrors();

		assertThat(result.pick("contextValue"), is(true));
	}

	public class Root
	{
		@GraphQLField
		public boolean direct(@GraphQLContext("test") Object test)
		{
			return "TestEnv".equals(test);
		}

		@GraphQLField
		public boolean indirect(@TestContext String test)
		{
			return "TestEnv".equals(test);
		}

		@GraphQLField
		public boolean optional(@GraphQLContext("test") Optional<Object> test)
		{
			return "TestEnv".equals(test.get());
		}

		@GraphQLField
		public boolean contextValue(@GraphQLContext("test") ContextValue<Object> test)
		{
			return "TestEnv".equals(test.get());
		}
	}

	@GraphQLContext("test")
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestContext
	{
	}
}
