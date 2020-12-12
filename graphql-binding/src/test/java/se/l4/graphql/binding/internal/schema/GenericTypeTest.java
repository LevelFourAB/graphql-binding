package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLMatchers;
import se.l4.graphql.binding.internal.GraphQLTest;

public class GenericTypeTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(StringHolderQueryType.class);
	}

	@Test
	public void testSchema()
	{
		GraphQLObjectType stringWrapper = schema.getObjectType("StringWrapper");
		assertThat(stringWrapper, notNullValue());

		GraphQLFieldDefinition field = stringWrapper.getFieldDefinition("item");
		assertThat(field, notNullValue());
		assertThat(field.getType(), GraphQLMatchers.isSameType(Scalars.GraphQLString));

		GraphQLObjectType stringHolderWrapper = schema.getObjectType("StringHolderQueryTypeWrapper");
		assertThat(stringHolderWrapper, notNullValue());
	}

	@Test
	public void testOutputScalar()
	{
		Result result = execute("{ outputScalar { item } }");
		result.assertNoErrors();

		assertThat(result.pick("outputScalar", "item"), is("hello"));
	}

	@Test
	public void testOutputObject()
	{
		Result result = execute("{ outputObject { item { value } } }");
		result.assertNoErrors();

		assertThat(result.pick("outputObject", "item", "value"), is("world"));
	}

	@Test
	public void testOutputConverting()
	{
		Result result = execute("{ outputConverting { item { value } } }");
		result.assertNoErrors();

		assertThat(result.pick("outputConverting", "item", "value"), is("conversion"));
	}

	public class Root
	{
		@GraphQLField
		public Wrapper<String> outputScalar()
		{
			return new Wrapper<>("hello");
		}

		@GraphQLField
		public Wrapper<Complex> outputObject()
		{
			return new Wrapper<>(new Complex());
		}

		@GraphQLField
		public Wrapper<StringHolder> outputConverting()
		{
			return new Wrapper<>(new StringHolder("conversion"));
		}
	}

	@GraphQLObject
	public class Wrapper<T>
	{
		public T item;

		public Wrapper(T item)
		{
			this.item = item;
		}

		@GraphQLField
		public T item()
		{
			return item;
		}
	}

	@GraphQLObject
	public class Complex
	{
		@GraphQLField
		public String value = "world";
	}

	public class StringHolder
	{
		private final String value;

		public StringHolder(String value)
		{
			this.value = value;
		}
	}

	@GraphQLObject
	public static class StringHolderQueryType
	{
		private final StringHolder value;

		@GraphQLFactory
		public StringHolderQueryType(@GraphQLSource StringHolder value)
		{
			this.value = value;
		}

		@GraphQLField
		public String value()
		{
			return value.value;
		}
	}
}
