package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class TypeConversionTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder
			.withRoot(new Root())
			.withType(ActualSubType.class);
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

	public class Root
	{
		@GraphQLField
		public ConvertedType get()
		{
			return new ConvertedType("test");
		}

		@GraphQLField
		public List<ConvertedType> list()
		{
			return ImmutableList.of(new ConvertedType("v0"), new ConvertedType("v1"));
		}
	}

	private class ConvertedType
	{
		private final String id;

		public ConvertedType(String id)
		{
			this.id = id;
		}
	}

	@GraphQLObject
	public class ActualSubType
	{
		private final ConvertedType data;

		@GraphQLFactory
		public ActualSubType(@GraphQLSource ConvertedType data)
		{
			this.data = data;
		}

		@GraphQLField
		public String id()
		{
			return data.id;
		}
	}
}
