package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLMixinField;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class InterfaceMixinTest
	extends GraphQLTest
{
	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(Impl.class);
	}

	@Test
	public void test()
	{
		Result result = execute("{ output { name } }");
		result.assertNoErrors();

		assertThat(result.pick("output", "name"), is("test"));
	}

	public class Root
	{
		@GraphQLMixinField
		public String name(@GraphQLSource Interface i)
		{
			return "test";
		}

		@GraphQLField
		@GraphQLNonNull
		public Interface output()
		{
			return new Impl();
		}
	}

	@GraphQLInterface
	public interface Interface
	{
	}

	@GraphQLObject
	public class Impl implements Interface
	{
	}
}
