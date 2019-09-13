package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLConvertFrom;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class InterfaceConversionTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder
			.withRoot(new Root())
			.withType(GraphQLOverTestInterface.class)
			.withType(GraphQLOverTypeA.class)
			.withType(GraphQLOverTypeB.class);
	}

	@Test
	public void testDirect()
	{
		Result result = execute("{ get { id } }");
		result.assertNoErrors();

		assertThat(result.pick("get", "id"), is("test"));
	}

	@Test
	public void testList()
	{
		Result result = execute("{ list { id } }");
		result.assertNoErrors();

		assertThat(result.pick("list", "0", "id"), is("v0"));
		assertThat(result.pick("list", "1", "id"), is("v1"));
	}

	@Test
	public void testListExtra()
	{
		Result result = execute("{ list { id ... on GraphQLOverTypeB { extra } } }");
		result.assertNoErrors();

		assertThat(result.pick("list", "0", "id"), is("v0"));
		assertThat(result.pick("list", "1", "id"), is("v1"));
		assertThat(result.pick("list", "1", "extra"), is(10));
	}

	public class Root
	{
		@GraphQLField
		public TestInterface get()
		{
			return new TypeA("test");
		}

		@GraphQLField
		public List<TestInterface> list()
		{
			return ImmutableList.of(new TypeA("v0"), new TypeB("v1"));
		}
	}

	private interface TestInterface
	{
		String id();
	}

	private interface NotShared
	{
	}

	private class TypeA
		implements TestInterface, NotShared
	{
		private final String id;

		public TypeA(String id)
		{
			this.id = id;
		}

		@Override
		public String id()
		{
			return this.id;
		}
	}

	private class TypeB
		implements TestInterface
	{
		private final String id;

		public TypeB(String id)
		{
			this.id = id;
		}

		@Override
		public String id()
		{
			return this.id;
		}
	}


	@GraphQLInterface
	@GraphQLConvertFrom(TestInterface.class)
	public interface GraphQLOverTestInterface
	{
		@GraphQLField
		String id();
	}

	@GraphQLObject
	public class GraphQLOverTypeA
		implements GraphQLOverTestInterface
	{
		private final TypeA data;

		@GraphQLFactory
		public GraphQLOverTypeA(@GraphQLSource TypeA data)
		{
			this.data = data;
		}

		@GraphQLField
		public String id()
		{
			return data.id;
		}
	}

	@GraphQLObject
	public class GraphQLOverTypeB
		implements GraphQLOverTestInterface
	{
		private final TypeB data;

		@GraphQLFactory
		public GraphQLOverTypeB(@GraphQLSource TypeB data)
		{
			this.data = data;
		}

		@Override
		public String id()
		{
			return data.id;
		}

		@GraphQLField
		public int extra()
		{
			return 10;
		}
	}
}
