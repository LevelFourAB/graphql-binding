package se.l4.graphql.binding.internal.schema.directives;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import graphql.introspection.Introspection.DirectiveLocation;
import graphql.schema.GraphQLDirective;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.GraphQLTest;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveCreationEncounter;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldEncounter;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldResolver;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveResolver;

public class FieldDirectiveTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withDirective(new TestDirectiveResolver());
	}

	@Test
	public void testApplied()
	{
		Result result = execute("{ doThing }");
		result.assertNoErrors();

		assertThat(result.pick("doThing"), is("testsuffix"));
	}

	public class Root
	{
		@GraphQLField
		@TestDirective
		public String doThing()
		{
			return "test";
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestDirective
	{
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
				.build();
		}

		@Override
		public void applyField(GraphQLDirectiveFieldEncounter<TestDirective> encounter)
		{
			DataFetchingSupplier<?> current = encounter.getSupplier();
			encounter.setSupplier(env -> {
				return current.get(env) + "suffix";
			});
		}
	}
}
