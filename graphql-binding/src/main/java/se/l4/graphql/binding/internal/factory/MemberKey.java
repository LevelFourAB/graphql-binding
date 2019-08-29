package se.l4.graphql.binding.internal.factory;

import java.util.Arrays;

import se.l4.commons.types.reflect.ExecutableRef;
import se.l4.commons.types.reflect.MemberRef;

/**
 * A key representing a {@link MemberRef}. Use during type resolving to keep
 * track of members that have been processed.
 */
public class MemberKey
{
	private final String name;
	private final Class<?>[] parameters;

	private MemberKey(String name, Class<?>[] parameters)
	{
		this.name = name;
		this.parameters = parameters;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode() ^ Arrays.hashCode(parameters);
	}

	@Override
	public boolean equals(Object o)
	{
		if(! (o instanceof MemberKey))
		{
			return false;
		}

		MemberKey other = (MemberKey) o;
		return other.name.equals(name)
			&& Arrays.equals(other.parameters, parameters);
	}

	public static MemberKey create(MemberRef member)
	{
		return new MemberKey(
			member.getName(),
			member instanceof ExecutableRef
				? ((ExecutableRef) member).getExecutable().getParameterTypes()
				: null
		);
	}
}
