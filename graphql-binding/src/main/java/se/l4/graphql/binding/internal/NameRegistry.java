package se.l4.graphql.binding.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import se.l4.commons.types.reflect.MemberRef;
import se.l4.commons.types.reflect.ParameterRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.GraphQLMappingException;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.resolver.Breadcrumb;

public class NameRegistry
{
	private final Map<TypeRef, String> typeNames;
	private final Map<String, Breadcrumb> typeReverseNames;

	public NameRegistry()
	{
		this.typeNames = new HashMap<>();
		this.typeReverseNames = new HashMap<>();
	}

	/**
	 * Get the name the given type has in the schema.
	 *
	 * @param type
	 * @return
	 */
	public Optional<String> getName(TypeRef type)
	{
		return Optional.ofNullable(typeNames.get(type.withoutUsage()));
	}

	public boolean hasName(String name)
	{
		return typeReverseNames.containsKey(name);
	}

	public void reserveName(
		String name,
		Breadcrumb crumb,
		TypeRef... refs
	)
	{
		Breadcrumb currentCrumb = typeReverseNames.get(name);
		if(currentCrumb != null)
		{
			throw new GraphQLMappingException("Name `" + name + "` has already been reserved " + currentCrumb.getLocation());
		}

		typeReverseNames.put(name, crumb);

		for(TypeRef ref : refs)
		{
			typeNames.put(ref.withoutUsage(), name);
		}
	}

	public void reserveNameAllowDuplicate(TypeRef type, String name)
	{
		if(typeNames.containsKey(type)) return;

		typeNames.put(type.withoutUsage(), name);
	}

	/**
	 * Get the name of the given member.
	 *
	 * @param member
	 * @return
	 */
	public String getName(MemberRef member)
	{
		Optional<GraphQLName> name = member.findAnnotation(GraphQLName.class);
		if(name.isPresent())
		{
			return name.get().value();
		}

		return member.getName();
	}

	public String getName(ParameterRef parameter)
	{
		Optional<GraphQLName> name = parameter.findAnnotation(GraphQLName.class);
		if(name.isPresent())
		{
			return name.get().value();
		}

		if(parameter.isNamePresent())
		{
			return parameter.getName().get();
		}

		throw new GraphQLMappingException("Unable to resolve name for parameter, needs to be annotated with @GraphQLName");
	}
}
