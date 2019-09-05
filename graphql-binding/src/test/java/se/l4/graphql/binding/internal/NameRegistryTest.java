package se.l4.graphql.binding.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import se.l4.commons.types.Types;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.annotations.GraphQLName;

public class NameRegistryTest
{
	private NameRegistry registry;

	@Before
	public void setup()
	{
		registry = new NameRegistry();
	}

	@Test
	public void testTypeNoAnnotation()
	{
		String name = registry.getName(Types.reference(TypeNoAnnotation.class));
		assertThat(name, is("TypeNoAnnotation"));
	}

	@Test
	public void testTypeAnnotated()
	{
		String name = registry.getName(Types.reference(TypeAnnotated.class));
		assertThat(name, is("Type"));
	}

	@Test
	public void testGetSeveralTimes()
	{
		String name1 = registry.getName(Types.reference(TypeNoAnnotation.class));
		assertThat(name1, is("TypeNoAnnotation"));

		String name2 = registry.getName(Types.reference(TypeNoAnnotation.class));
		assertThat(name2, is("TypeNoAnnotation"));
	}

	@Test
	public void testTypeAnnotatedDuplicateName()
	{
		String name = registry.getName(Types.reference(TypeAnnotated.class));
		assertThat(name, is("Type"));

		try
		{
			registry.getName(Types.reference(TypeAnnotated2.class));
		}
		catch(GraphQLMappingException e)
		{
			// This exception is triggered due to the duplicate name
			return;
		}

		fail();
	}

	@Test
	public void testTypeGeneric()
	{
		String name = registry.getName(Types.reference(
			TypeGeneric.class,
			TypeNoAnnotation.class
		));
		assertThat(name, is("TypeNoAnnotationTypeGeneric"));
	}

	@Test
	public void testTypeGenericAnnotated()
	{
		String name = registry.getName(Types.reference(
			TypeGenericAnnotated.class,
			TypeNoAnnotation.class
		));
		assertThat(name, is("TypeNoAnnotationGeneric"));
	}

	class TypeNoAnnotation {
	}

	@GraphQLName("Type")
	class TypeAnnotated {
	}

	@GraphQLName("Type")
	class TypeAnnotated2 {
	}

	class TypeGeneric<T> {
	}

	@GraphQLName("Generic")
	class TypeGenericAnnotated<T> {
	}
}
