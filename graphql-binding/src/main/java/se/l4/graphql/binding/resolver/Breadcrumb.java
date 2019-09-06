package se.l4.graphql.binding.resolver;

import se.l4.commons.types.reflect.MemberRef;
import se.l4.commons.types.reflect.TypeRef;

/**
 * Breadcrumb used when resolving types to make error messages nicer.
 */
public class Breadcrumb
{
	private final String message;

	private Breadcrumb(String message)
	{
		this.message = message;
	}

	/**
	 * Get the location described by this breadcrumb.
	 */
	public String getLocation()
	{
		return message;
	}

	public Breadcrumb then(Breadcrumb other)
	{
		if(this.message == null)
		{
			return other;
		}
		else if(other.message == null)
		{
			return this;
		}

		return new Breadcrumb(other.message + "\n  " + this.message);
	}

	/**
	 * Create a breadcrumb for the given type.
	 *
	 * @param ref
	 * @return
	 */
	public static Breadcrumb forType(TypeRef ref)
	{
		return new Breadcrumb("in `" + ref.toTypeName() + "`");
	}

	/**
	 * Create a breadcrumb for the given type.
	 *
	 * @param ref
	 * @return
	 */
	public static Breadcrumb forResolver(TypeRef ref, GraphQLResolver resolver)
	{
		return new Breadcrumb("in `" + ref.toTypeName() + "` via " + resolver.toString());
	}


	/**
	 * Create a breadcrumb for the given member.
	 *
	 * @param ref
	 * @return
	 */
	public static Breadcrumb forMember(MemberRef ref)
	{
		return new Breadcrumb("at `" + ref.toDescription() + "` in `" + ref.getDeclaringType().toTypeName() + "`");
	}

	/**
	 * Create a breadcrumb using a custom message.
	 *
	 * @param message
	 * @return
	 */
	public static Breadcrumb custom(String message)
	{
		return new Breadcrumb(message);
	}

	public static Breadcrumb empty()
	{
		return new Breadcrumb(null);
	}
}
