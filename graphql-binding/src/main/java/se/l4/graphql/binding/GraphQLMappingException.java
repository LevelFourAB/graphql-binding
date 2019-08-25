package se.l4.graphql.binding;

public class GraphQLMappingException
	extends RuntimeException
{

	public GraphQLMappingException(String message)
	{
		super(message);
	}

	public GraphQLMappingException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
