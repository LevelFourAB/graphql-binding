package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.OptionalInt;

import org.junit.Test;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.GraphQLTest;

public class OptionalIntTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testOutputEmpty()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputEmpty").getType();
		assertThat(type, is(Scalars.GraphQLInt));

		Result result = execute("{ outputEmpty }");
		result.assertNoErrors();

		assertThat(result.pick("outputEmpty"), nullValue());
	}

	@Test
	public void testOutputInteger()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputInteger").getType();
		assertThat(type, is(Scalars.GraphQLInt));

		Result result = execute("{ outputInteger }");
		result.assertNoErrors();

		assertThat(result.pick("outputInteger"), is(4));
	}

	@Test
	public void testOutputIntegerNonNull()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputIntegerNonNull").getType();
		assertThat(type, is(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLInt)));

		Result result = execute("{ outputIntegerNonNull }");
		result.assertNoErrors();

		assertThat(result.pick("outputIntegerNonNull"), is(5));
	}

	@Test
	public void testInputSchema()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("input")
			.getArgument("value")
			.getType();

		assertThat(type, is(Scalars.GraphQLInt));
	}

	@Test
	public void testInputSkipped()
	{
		Result result = execute("{ input }");
		result.assertNoErrors();

		assertThat(result.pick("input"), nullValue());
	}

	@Test
	public void testInputEmpty()
	{
		Result result = execute("{ input(value: null) }");
		result.assertNoErrors();

		assertThat(result.pick("input"), nullValue());
	}

	@Test
	public void testInputValue()
	{
		Result result = execute("{ input(value: 4) }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is(4));
	}

	@Test
	public void testInputNonNull()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("inputNonNull")
			.getArgument("value")
			.getType();

		assertThat(type, is(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLInt)));

		Result result = execute("{ inputNonNull(value: 6) }");
		result.assertNoErrors();

		assertThat(result.pick("inputNonNull"), is(6));
	}

	public static class Root
	{
		@GraphQLField
		public OptionalInt outputEmpty()
		{
			return OptionalInt.empty();
		}

		@GraphQLField
		public OptionalInt outputInteger()
		{
			return OptionalInt.of(4);
		}

		@GraphQLField
		@GraphQLNonNull
		public OptionalInt outputIntegerNonNull()
		{
			return OptionalInt.of(5);
		}

		@GraphQLField
		public Integer input(
			@GraphQLName("value") OptionalInt value
		)
		{
			return value.isPresent() ? value.getAsInt() : null;
		}

		@GraphQLField
		public Integer inputNonNull(
			@GraphQLNonNull @GraphQLName("value") OptionalInt value
		)
		{
			return value.isPresent() ? value.getAsInt() : null;
		}
	}
}
