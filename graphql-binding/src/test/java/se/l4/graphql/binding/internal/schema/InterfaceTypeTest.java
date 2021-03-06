package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLDeprecated;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInterface;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.internal.GraphQLMatchers;
import se.l4.graphql.binding.internal.GraphQLTest;

public class InterfaceTypeTest
	extends GraphQLTest
{
	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(Impl.class);
	}

	@Test
	public void testSchema()
	{
		GraphQLInterfaceType type = (GraphQLInterfaceType) schema.getType("Interface");

		GraphQLFieldDefinition nameDef = type.getFieldDefinition("name");
		assertThat(nameDef.getType(), GraphQLMatchers.isSameType(Scalars.GraphQLString));

		GraphQLFieldDefinition lastNameDef = type.getFieldDefinition("lastName");
		assertThat(lastNameDef.getDeprecationReason(), is("use name instead"));
	}

	@Test
	public void testNull()
	{
		Result result = execute("{ outputNull { name } }");
		result.assertNoErrors();

		assertThat(result.pick("outputNull"), is((Object) null));
	}

	@Test
	public void testNonNull()
	{
		Result result = execute("{ outputNonNull { name } }");
		result.assertNoErrors();

		assertThat(result.pick("outputNonNull", "name"), is("test"));
	}

	public class Root
	{
		@GraphQLField
		public Interface outputNull()
		{
			return null;
		}

		@GraphQLField
		public Interface outputNonNull()
		{
			return new Impl();
		}
	}

	@GraphQLInterface
	public interface Interface
	{
		@GraphQLField
		String name();

		@GraphQLDeprecated("use name instead")
		@GraphQLField
		String lastName();
	}

	@GraphQLObject
	public class Impl implements Interface
	{
		@Override
		public String name()
		{
			return "test";
		}

		@Override
		public String lastName()
		{
			return "test";
		}
	}
}
