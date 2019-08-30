package se.l4.graphql.binding.internal.schema;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLName;

public class SpecificInputTest
{
	@Test
	public void testShouldNotResolveBAsInput()
	{
		try
		{
			GraphQLBinder.newBinder()
				.withRoot(new RootWithB())
				.build();
		}
		catch(GraphQLMappingException e)
		{
			return;
		}

		fail("Expected failure in resolving");
	}

	@Test
	public void testShouldNotResolveArrayList()
	{
		try
		{
			GraphQLBinder.newBinder()
				.withRoot(new RootWithList())
				.build();
		}
		catch(GraphQLMappingException e)
		{
			return;
		}

		fail("Expected failure in resolving");
	}

	public static class RootWithB
	{
		@GraphQLField
		public String get(
			@GraphQLName("input") B in
		) {
			return null;
		}
	}

	@GraphQLInputObject
	public static class A {
	}

	public static class B extends A {
	}

	public static class RootWithList
	{
		@GraphQLField
		public String get(
			@GraphQLName("input") ArrayList<A> in
		) {
			return null;
		}
	}

}
