package se.l4.graphql.binding.internal.schema;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLEnum;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class EnumConversionTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(GraphQLOverTestEnum.class);
	}

	@Test
	public void testA()
	{
		Result result = execute("{ a }");
		result.assertNoErrors();
	}

	public class Root
	{
		@GraphQLField
		public TestEnum a()
		{
			return TestEnum.A;
		}

		@GraphQLField
		public TestEnum b()
		{
			return TestEnum.B;
		}
	}

	public enum TestEnum
	{
		A,
		B;
	}

	@GraphQLEnum
	public enum GraphQLOverTestEnum
	{
		A,
		B;

		@GraphQLFactory
		public static GraphQLOverTestEnum create(@GraphQLSource TestEnum test)
		{
			switch(test)
			{
				case A:
					return GraphQLOverTestEnum.A;
				case B:
					return GraphQLOverTestEnum.B;
			}

			throw new AssertionError();
		}
	}
}
