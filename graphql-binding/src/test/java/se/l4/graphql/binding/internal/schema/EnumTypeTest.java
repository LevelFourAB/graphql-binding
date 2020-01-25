package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLDeprecated;
import se.l4.graphql.binding.annotations.GraphQLEnum;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.internal.GraphQLTest;

public class EnumTypeTest
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
		GraphQLEnumType type = (GraphQLEnumType) schema.getType("TestEnum");

		GraphQLEnumValueDefinition def = type.getValue("DEPRECATED");
		assertThat(def.getDeprecationReason(), is(""));
	}

	@Test
	public void testOutput()
	{
		Result result = execute("{ output }");
		result.assertNoErrors();

		assertThat(result.pick("output"), is("VALUE"));
	}

	@Test
	public void testOutputNamed()
	{
		Result result = execute("{ outputNamed }");
		result.assertNoErrors();

		assertThat(result.pick("outputNamed"), is("ValueWithName"));
	}

	@Test
	public void testInput()
	{
		Result result = execute("{ input(value: ValueWithName) }");
		result.assertNoErrors();

		assertThat(result.pick("input"), is("VALUE_WITH_NAME"));
	}

	public class Root
	{
		@GraphQLField
		public TestEnum output()
		{
			return TestEnum.VALUE;
		}

		@GraphQLField
		public TestEnum outputNamed()
		{
			return TestEnum.VALUE_WITH_NAME;
		}

		@GraphQLField
		public String input(
			@GraphQLName("value") TestEnum e
		)
		{
			return e.name();
		}
	}

	@GraphQLEnum
	public enum TestEnum
	{
		VALUE,

		@GraphQLName("ValueWithName")
		VALUE_WITH_NAME,

		@GraphQLDeprecated
		DEPRECATED;
	}
}
