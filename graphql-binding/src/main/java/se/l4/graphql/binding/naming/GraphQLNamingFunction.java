package se.l4.graphql.binding.naming;

/**
 * Function that can be used to name a type.
 */
public interface GraphQLNamingFunction
{
	/**
	 * Computer the name for the given encounter.
	 *
	 * @param encounter
	 *   encounter with context and type information
	 * @return
	 *   computer name
	 */
	String compute(GraphQLNamingEncounter encounter);
}
