package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLType;
import se.l4.graphql.binding.internal.GraphQLTest;

public class InterfaceTypeTest
	extends GraphQLTest
{
	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(Impl.class);
	}

	@Test
	public void testNull()
	{
		Result result = execute("{ outputNull { name } }");
		result.assertNoErrors();

		assertThat(result.pick("outputNull"), is((Object) null));
	}

	@Test
	public void testNonNull()
	{
		Result result = execute("{ outputNonNull { name } }");
		result.assertNoErrors();

		assertThat(result.pick("outputNonNull", "name"), is("test"));
	}

	public class Root
	{
		@GraphQLField
		public Interface outputNull()
		{
			return null;
		}

		@GraphQLField
		public Interface outputNonNull()
		{
			return new Impl();
		}
	}

	@GraphQLInterface
	public interface Interface
	{
		@GraphQLField
		String name();
	}

	@GraphQLType
	public class Impl implements Interface
	{
		@Override
		public String name() {
			return "test";
		}
	}
}
