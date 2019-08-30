package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.OptionalDouble;

import org.junit.Test;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.GraphQLTest;

public class OptionalDoubleTest
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
		assertThat(type, is(Scalars.GraphQLFloat));

		Result result = execute("{ outputEmpty }");
		result.assertNoErrors();

		assertThat(result.pick("outputEmpty"), nullValue());
	}

	@Test
	public void testOutputDouble()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputDouble").getType();
		assertThat(type, is(Scalars.GraphQLFloat));

		Result result = execute("{ outputDouble }");
		result.assertNoErrors();

		assertThat(result.pick("outputDouble"), is(4.0));
	}

	@Test
	public void testOutputDoubleNonNull()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputDoubleNonNull").getType();
		assertThat(type, is(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLFloat)));

		Result result = execute("{ outputDoubleNonNull }");
		result.assertNoErrors();

		assertThat(result.pick("outputDoubleNonNull"), is(5.0));
	}

	@Test
	public void testInputSchema()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("input")
			.getArgument("value")
			.getType();

		assertThat(type, is(Scalars.GraphQLFloat));
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
		Result result = execute("{ input(value: 4.2) }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is(4.2));
	}

	@Test
	public void testInputNonNull()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("inputNonNull")
			.getArgument("value")
			.getType();

		assertThat(type, is(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLFloat)));

		Result result = execute("{ inputNonNull(value: 6019.2) }");
		result.assertNoErrors();

		assertThat(result.pick("inputNonNull"), is(6019.2));
	}

	public static class Root
	{
		@GraphQLField
		public OptionalDouble outputEmpty()
		{
			return OptionalDouble.empty();
		}

		@GraphQLField
		public OptionalDouble outputDouble()
		{
			return OptionalDouble.of(4);
		}

		@GraphQLField
		@GraphQLNonNull
		public OptionalDouble outputDoubleNonNull()
		{
			return OptionalDouble.of(5);
		}

		@GraphQLField
		public Double input(
			@GraphQLName("value") OptionalDouble value
		)
		{
			return value.isPresent() ? value.getAsDouble() : null;
		}

		@GraphQLField
		public Double inputNonNull(
			@GraphQLNonNull @GraphQLName("value") OptionalDouble value
		)
		{
			return value.isPresent() ? value.getAsDouble() : null;
		}
	}
}
