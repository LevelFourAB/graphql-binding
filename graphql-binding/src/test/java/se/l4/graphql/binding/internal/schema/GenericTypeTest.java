package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.internal.GraphQLTest;

public class GenericTypeTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testSchema()
	{
		GraphQLObjectType stringWrapper = schema.getObjectType("Wrapper_String");
		assertThat(stringWrapper, notNullValue());

		GraphQLFieldDefinition field = stringWrapper.getFieldDefinition("item");
		assertThat(field, notNullValue());
		assertThat(field.getType(), is(Scalars.GraphQLString));
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
}
