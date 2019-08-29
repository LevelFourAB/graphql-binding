package se.l4.graphql.binding.internal;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	public String getName(TypeRef type)
	{
		return getName(type, new HashSet<>());
	}

	protected String getName(TypeRef type, Set<Type> path)
	{
		Class<?> erasedType = type.getErasedType();

		// Protect against potential type loops
		if(path.contains(erasedType))
		{
			throw new GraphQLMappingException("Unable to resolve name, `"
				+ type.toTypeName() + "` references itself"
			);
		}

		path.add(erasedType);

		// Get the registered type name if available
		String name = typeNames.get(type.withoutUsage());
		if(name != null) return name;

		// Find a proposed name, either via annotation or using the simple type name
		GraphQLName nameAnnotation = erasedType.getAnnotation(GraphQLName.class);
		String proposedName = nameAnnotation == null ? erasedType.getSimpleName() : nameAnnotation.value();

		// Go through and add any type parameters to the name
		StringBuilder nameBuilder = new StringBuilder()
			.append(proposedName);

		for(TypeRef typeParam : type.getTypeParameters())
		{
			String subName = getName(typeParam, path);
			nameBuilder.append("_").append(subName);
		}

		name = nameBuilder.toString();

		// Check that the name is unique and register it
		Breadcrumb existing = typeReverseNames.get(name);
		if(existing != null)
		{
			throw new GraphQLMappingException("Name collision for `"
				+ type.toTypeName() + "`, name resolved to `" + name
				+ "` but name was previously resolved " + existing.getLocation()
			);
		}

		typeReverseNames.put(name, Breadcrumb.forType(type));
		typeNames.put(type.withoutUsage(), name);

		return name;
	}

	public boolean hasName(String name)
	{
		return typeReverseNames.containsKey(name);
	}

	public void reserveName(
		String name,
		Breadcrumb crumb,
		TypeRef... refs)
	{
		if(hasName(name))
		{
			throw new GraphQLMappingException("Name `" + name + "` has already been reserved");
		}

		typeReverseNames.put(name, crumb);

		for(TypeRef ref : refs)
		{
			typeNames.put(ref.withoutUsage(), name);
		}
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
