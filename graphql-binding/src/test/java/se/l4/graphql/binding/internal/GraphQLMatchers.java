package se.l4.graphql.binding.internal;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;

public class GraphQLMatchers
{
	private GraphQLMatchers()
	{
	}

	public static Matcher<? super GraphQLType> isSameType(GraphQLType type)
	{
		return new TypeSafeMatcher<GraphQLType>(GraphQLType.class)
		{
			@Override
			protected boolean matchesSafely(GraphQLType item)
			{
				return match(type, item);
			}

			@Override
			protected void describeMismatchSafely(
				GraphQLType item,
				Description mismatchDescription
			)
			{
				mismatchDescription
					.appendText("was ")
					.appendValue(item)
					.appendText(" expected ")
					.appendValue(type);
			}

			@Override
			public void describeTo(Description description)
			{
				description
					.appendText("the GraphQL type ")
					.appendValue(type);
			}
		};
	}

	public static boolean match(GraphQLType a, GraphQLType b)
	{
		if(a.equals(b)) return true;

		if(a instanceof GraphQLNonNull && b instanceof GraphQLNonNull)
		{
			return match(
				((GraphQLNonNull) a).getWrappedType(),
				((GraphQLNonNull) b).getWrappedType()
			);
		}
		else if(a instanceof GraphQLList && b instanceof GraphQLList)
		{
			return match(
				((GraphQLList) a).getWrappedType(),
				((GraphQLList) b).getWrappedType()
			);
		}

		return false;
	}
}
