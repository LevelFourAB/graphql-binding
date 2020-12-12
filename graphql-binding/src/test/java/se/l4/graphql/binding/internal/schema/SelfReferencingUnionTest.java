package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLUnionType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLUnion;
import se.l4.graphql.binding.internal.GraphQLTest;

/**
 * This test is for unions, where a type references a union that it is part
 * of.
 */
public class SelfReferencingUnionTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(InUnionObject.class);
	}

	@Test
	public void testSchema()
	{
		GraphQLUnionType type = (GraphQLUnionType) schema.getType("Union");
		assertThat(type, notNullValue());
		assertThat(type.getTypes(), hasItem(schema.getObjectType("InUnionObject")));
	}

	public class Root
	{
		@GraphQLField
		public InUnionObject get(
			@GraphQLName("name") String name
		)
		{
			return new InUnionObject(name);
		}
	}

	@GraphQLObject
	public class InUnionObject
		implements Union
	{
		private final String name;

		public InUnionObject(String name)
		{
			this.name = name;
		}

		@GraphQLField
		@GraphQLNonNull
		public String name()
		{
			return name;
		}

		@GraphQLField
		public Union parent()
		{
			return null;
		}
	}

	@GraphQLUnion
	public interface Union
	{
	}
}
