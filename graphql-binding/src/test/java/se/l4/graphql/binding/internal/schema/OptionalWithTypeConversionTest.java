package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class OptionalWithTypeConversionTest
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

	public class Root
	{
		@GraphQLField
		public Optional<ConvertedType> get()
		{
			return Optional.of(new ConvertedType("test"));
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
