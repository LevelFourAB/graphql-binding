package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

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

public class OptionalTest
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
		assertThat(type, GraphQLMatchers.isSameType(Scalars.GraphQLString));

		Result result = execute("{ outputEmpty }");
		result.assertNoErrors();

		assertThat(result.pick("outputEmpty"), nullValue());
	}

	@Test
	public void testOutputString()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputString").getType();
		assertThat(type, GraphQLMatchers.isSameType(Scalars.GraphQLString));

		Result result = execute("{ outputString }");
		result.assertNoErrors();

		assertThat(result.pick("outputString"), is("test"));
	}

	@Test
	public void testOutputStringNonNull()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputStringNonNull").getType();
		assertThat(type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString)));

		Result result = execute("{ outputStringNonNull }");
		result.assertNoErrors();

		assertThat(result.pick("outputStringNonNull"), is("test"));
	}

	@Test
	public void testOutputStringNonNullOuter()
	{
		GraphQLOutputType type = schema.getQueryType().getFieldDefinition("outputStringNonNullOuter").getType();
		assertThat(type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString)));

		Result result = execute("{ outputStringNonNullOuter }");
		result.assertNoErrors();

		assertThat(result.pick("outputStringNonNullOuter"), is("test"));
	}

	@Test
	public void testInputSchema()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("input")
			.getArgument("value")
			.getType();

		assertThat(type, GraphQLMatchers.isSameType(Scalars.GraphQLString));
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
		Result result = execute("{ input(value: \"value\") }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is("value"));
	}

	@Test
	public void testInputNonNull()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("inputNonNull")
			.getArgument("value")
			.getType();

		assertThat(type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString)));

		Result result = execute("{ inputNonNull(value: \"value\") }");
		result.assertNoErrors();

		assertThat(result.pick("inputNonNull"), is("value"));
	}


	@Test
	public void testInputNonNull2()
	{
		GraphQLInputType type = schema.getQueryType()
			.getFieldDefinition("inputNonNull2")
			.getArgument("value")
			.getType();

		assertThat(type, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString)));

		Result result = execute("{ inputNonNull2(value: \"value\") }");
		result.assertNoErrors();

		assertThat(result.pick("inputNonNull2"), is("value"));
	}

	public static class Root
	{
		@GraphQLField
		public Optional<String> outputEmpty()
		{
			return Optional.empty();
		}

		@GraphQLField
		public Optional<String> outputString()
		{
			return Optional.of("test");
		}

		@GraphQLField
		public Optional<@GraphQLNonNull String> outputStringNonNull()
		{
			return Optional.of("test");
		}

		@GraphQLField
		@GraphQLNonNull
		public Optional<String> outputStringNonNullOuter()
		{
			return Optional.of("test");
		}

		@GraphQLField
		public String input(
			@GraphQLName("value") Optional<String> value
		)
		{
			return value.orElse(null);
		}

		@GraphQLField
		public String inputNonNull(
			@GraphQLNonNull @GraphQLName("value") Optional<String> value
		)
		{
			return value.orElse(null);
		}

		@GraphQLField
		public String inputNonNull2(
			@GraphQLName("value") Optional<@GraphQLNonNull String> value
		)
		{
			return value.orElse(null);
		}
	}
}
