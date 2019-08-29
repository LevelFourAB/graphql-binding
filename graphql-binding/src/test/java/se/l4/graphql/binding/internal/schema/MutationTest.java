package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLMutation;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.internal.GraphQLTest;

public class MutationTest
	extends GraphQLTest
{
	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testScalarMutation()
	{
		Result result = execute("mutation($in: String) { mutateWithScalar(input: $in) }",
			ImmutableMap.of("in", "test")
		);

		result.assertNoErrors();

		assertThat(result.pick("mutateWithScalar"), is("test"));
	}

	public class Root
	{
		@GraphQLMutation
		public String mutateWithScalar(
			@GraphQLName("input") String input
		)
		{
			return input;
		}
	}
}
