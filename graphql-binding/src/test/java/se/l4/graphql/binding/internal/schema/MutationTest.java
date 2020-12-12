package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
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
			Map.of("in", "test")
		);

		result.assertNoErrors();

		assertThat(result.pick("mutateWithScalar"), is("test"));
	}

	public class Root
	{
		@GraphQLField
		public String none()
		{
			return null;
		}

		@GraphQLMutation
		public String mutateWithScalar(
			@GraphQLName("input") String input
		)
		{
			return input;
		}
	}
}
