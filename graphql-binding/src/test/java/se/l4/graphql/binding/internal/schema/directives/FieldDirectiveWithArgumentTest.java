package se.l4.graphql.binding.internal.schema.directives;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import graphql.Scalars;
import graphql.introspection.Introspection.DirectiveLocation;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.GraphQLTest;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveCreationEncounter;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldEncounter;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldResolver;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveResolver;

public class FieldDirectiveWithArgumentTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withDirective(new TestDirectiveResolver());
	}


	@Test
	public void testSchema()
	{
		GraphQLDirective directive = schema.getDirective("Directive");
		assertThat(directive, notNullValue());

		GraphQLFieldDefinition def = schema.getQueryType().getFieldDefinition("doThing");
		GraphQLDirective fieldDirective = def.getDirective("Directive");
		assertThat(fieldDirective, notNullValue());
		assertThat(fieldDirective.getArgument("test").getValue(), is("1234"));
	}

	@Test
	public void testApplied()
	{
		Result result = execute("{ doThing }");
		result.assertNoErrors();

		assertThat(result.pick("doThing"), is("test"));
	}

	public class Root
	{
		@GraphQLField
		@TestDirective("1234")
		public String doThing()
		{
			return "test";
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestDirective
	{
		String value();
	}

	public class TestDirectiveResolver
		implements GraphQLDirectiveResolver<TestDirective>,
			GraphQLDirectiveFieldResolver<TestDirective>
	{
		@Override
		public GraphQLDirective createDirective(GraphQLDirectiveCreationEncounter encounter)
		{
			return GraphQLDirective.newDirective()
				.name("Directive")
				.validLocation(DirectiveLocation.FIELD_DEFINITION)
				.argument(GraphQLArgument.newArgument()
					.name("test")
					.type(Scalars.GraphQLString)
					.build()
				)
				.build();
		}

		@Override
		public void applyField(GraphQLDirectiveFieldEncounter<TestDirective> encounter)
		{
			TestDirective d = encounter.getAnnotation();
			encounter.setArgument("test", d.value());
		}
	}
}
