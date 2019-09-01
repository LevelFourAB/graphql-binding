package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLScalar;
import se.l4.graphql.binding.internal.GraphQLTest;
import se.l4.graphql.binding.resolver.GraphQLScalarResolver;

public class AnnotatedScalarTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testOutput()
	{
		Result result = execute("{ output }");
		result.assertNoErrors();

		assertThat(result.pick("output"), is("Test"));
	}

	@Test
	public void testInputLiteral()
	{
		Result result = execute("{ input(id: \"1234\") }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is("1234"));
	}

	@Test
	public void testInputLiteralConverting()
	{
		Result result = execute("{ input(id: 1234) }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is("1234"));
	}

	@Test
	public void testInputVariables()
	{
		Result result = execute("query($id: OpaqueID) { input(id: $id) }",
			ImmutableMap.of("id", "1234")
		);
		result.assertNoErrors();

		assertThat(result.pick("input"), is("1234"));
	}

	@Test
	public void testInputVariablesConverting()
	{
		Result result = execute("query($id: OpaqueID) { input(id: $id) }",
			ImmutableMap.of("id", 1234)
		);
		result.assertNoErrors();

		assertThat(result.pick("input"), is("1234"));
	}

	public class Root
	{
		@GraphQLField
		public OpaqueID output()
		{
			return new OpaqueID("Test");
		}

		@GraphQLField
		public OpaqueID input(
			@GraphQLName("id") OpaqueID id
		)
		{
			return id;
		}
	}

	@GraphQLScalar(IDScalar.class)
	public static class OpaqueID
	{
		private final String value;

		public OpaqueID(String value)
		{
			this.value = value;
		}
	}

	public static class IDScalar
		implements GraphQLScalarResolver<OpaqueID, String>
	{

		@Override
		public String serialize(OpaqueID instance)
		{
			return instance.value;
		}

		@Override
		public OpaqueID parseValue(String input)
		{
			return new OpaqueID(input);
		}

	}
}
