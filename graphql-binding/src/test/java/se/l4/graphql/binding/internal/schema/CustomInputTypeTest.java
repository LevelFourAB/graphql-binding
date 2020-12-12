package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import graphql.Scalars;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.internal.GraphQLMatchers;
import se.l4.graphql.binding.internal.GraphQLTest;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.TypedGraphQLInputResolver;
import se.l4.ylem.types.reflect.TypeRef;

public class CustomInputTypeTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder
			.withRoot(new Root())
			.withResolver(new MaybeResolver());
	}

	@Test
	public void testParameterSchema()
	{
		graphql.schema.GraphQLType nameType = schema.getQueryType().getFieldDefinition("asScalarParameter")
			.getArgument("name")
			.getType();

		assertThat(nameType, GraphQLMatchers.isSameType(Scalars.GraphQLString));

		graphql.schema.GraphQLType nameType2 = schema.getQueryType().getFieldDefinition("asScalarParameterNonNull")
			.getArgument("name")
			.getType();

		assertThat(nameType2, GraphQLMatchers.isSameType(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString)));
	}

	@Test
	public void testScalarParameter()
	{
		Result result = execute("{ asScalarParameter(name: \"Test\") }");
		result.assertNoErrors();

		assertThat(result.pick("asScalarParameter"), is("Test"));
	}

	@Test
	public void testComplexParameter()
	{
		Result result = execute("{ asComplexParameter(item: { name: \"Test\" }) }");
		result.assertNoErrors();

		assertThat(result.pick("asComplexParameter"), is("Test"));
	}

	public class Root
	{
		@GraphQLField
		public String asScalarParameter(
			@GraphQLName("name") Maybe<String> name
		)
		{
			return name.value;
		}

		@GraphQLField
		public String asScalarParameterNonNull(
			@GraphQLNonNull @GraphQLName("name") Maybe<String> name
		)
		{
			return name.value;
		}

		@GraphQLField
		public String asComplexParameter(
			@GraphQLName("item") Maybe<CustomType> item
		)
		{
			return item.value.name;
		}
	}

	@GraphQLInputObject
	public static class CustomType
	{
		@GraphQLField
		public String name;
	}

	public class Maybe<T>
	{
		private final T value;

		public Maybe(T o)
		{
			this.value = o;
		}
	}

	public class MaybeResolver
		implements TypedGraphQLInputResolver
	{
		@Override
		public Class<?> getType()
		{
			return Maybe.class;
		}

		@Override
		public ResolvedGraphQLType<? extends graphql.schema.GraphQLInputType> resolveInput(
			GraphQLInputEncounter encounter
		)
		{
			TypeRef value = encounter.getType().getTypeParameter(0).get();
			return encounter.getContext().resolveInput(value)
				.withInputConversion((env, s) -> new Maybe<>(s));
		}
	}
}
