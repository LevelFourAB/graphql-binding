package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.GraphQLMatchers;
import se.l4.graphql.binding.internal.GraphQLTest;

public class OptionalLongTest
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
		assertThat(type, GraphQLMatchers.isSameType(Scalars.GraphQLLong));

		Result result = execute("{ outputEmpty }");
		result.assertNoErrors();

		assertThat(result.pick("outputEmpty"), nullValue());
	}

	@Test
	public void testOutputLong()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputLong").getType();
		assertThat(type, GraphQLMatchers.isSameType(Scalars.GraphQLLong));

		Result result = execute("{ outputLong }");
		result.assertNoErrors();

		assertThat(result.pick("outputLong"), is(4l));
	}

	@Test
	public void testOutputLongNonNull()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputLongNonNull").getType();
		assertThat(type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLLong)));

		Result result = execute("{ outputLongNonNull }");
		result.assertNoErrors();

		assertThat(result.pick("outputLongNonNull"), is(5l));
	}

	@Test
	public void testInputSchema()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("input")
			.getArgument("value")
			.getType();

		assertThat(type, GraphQLMatchers.isSameType(Scalars.GraphQLLong));
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

		assertThat(result.pick("input"), is(4l));
	}

	@Test
	public void testInputNonNull()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("inputNonNull")
			.getArgument("value")
			.getType();

		assertThat(type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLLong)));

		Result result = execute("{ inputNonNull(value: 6) }");
		result.assertNoErrors();

		assertThat(result.pick("inputNonNull"), is(6l));
	}

	public static class Root
	{
		@GraphQLField
		public OptionalLong outputEmpty()
		{
			return OptionalLong.empty();
		}

		@GraphQLField
		public OptionalLong outputLong()
		{
			return OptionalLong.of(4);
		}

		@GraphQLField
		@GraphQLNonNull
		public OptionalLong outputLongNonNull()
		{
			return OptionalLong.of(5);
		}

		@GraphQLField
		public Long input(
			@GraphQLName("value") OptionalLong value
		)
		{
			return value.isPresent() ? value.getAsLong() : null;
		}

		@GraphQLField
		public Long inputNonNull(
			@GraphQLNonNull @GraphQLName("value") OptionalLong value
		)
		{
			return value.isPresent() ? value.getAsLong() : null;
		}
	}
}
