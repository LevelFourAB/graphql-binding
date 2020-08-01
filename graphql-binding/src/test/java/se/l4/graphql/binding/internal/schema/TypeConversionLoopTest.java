package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class TypeConversionLoopTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder
			.withRoot(new Root())
			.withType(GraphQLOverTypeA.class)
			.withType(GraphQLOverTypeB.class)
			.withType(GraphQLOverTypeC.class);
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
	public void testViaMethod()
	{
		Result result = execute("{ viaMethod { id } }");
		result.assertNoErrors();

		assertThat(result.pick("viaMethod", "id"), is("test"));
	}

	public class Root
	{
		@GraphQLField
		public TypeA get()
		{
			return new TypeA("test");
		}

		@GraphQLField
		public List<TypeA> list()
		{
			return List.of(new TypeA("v0"), new TypeA("v1"));
		}

		@GraphQLField
		public TypeB viaMethod()
		{
			return new TypeB("test");
		}
	}

	private class TypeA
	{
		private final String id;

		public TypeA(String id)
		{
			this.id = id;
		}
	}

	private class TypeB
	{
		private final String id;

		public TypeB(String id)
		{
			this.id = id;
		}
	}

	private class TypeC
	{
		private final String id;

		public TypeC(String id)
		{
			this.id = id;
		}
	}

	@GraphQLObject
	public class GraphQLOverTypeA
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

		@GraphQLField
		@GraphQLNonNull
		public List<@GraphQLNonNull TypeB> b()
		{
			return Collections.emptyList();
		}
	}

	@GraphQLObject
	public static class GraphQLOverTypeB
	{
		private final String id;

		@GraphQLFactory
		public GraphQLOverTypeB(@GraphQLSource TypeB data)
		{
			this.id = data.id;
		}

		@GraphQLField
		public String id()
		{
			return id;
		}

		@GraphQLField
		@GraphQLNonNull
		public List<@GraphQLNonNull TypeC> c()
		{
			return Collections.emptyList();
		}
	}

	@GraphQLObject
	public static class GraphQLOverTypeC
	{
		private final String id;

		@GraphQLFactory
		public GraphQLOverTypeC(@GraphQLSource TypeC data)
		{
			this.id = data.id;
		}

		@GraphQLField
		public String id()
		{
			return id;
		}

		@GraphQLField
		@GraphQLNonNull
		public List<@GraphQLNonNull TypeB> b()
		{
			return Collections.emptyList();
		}
	}
}
