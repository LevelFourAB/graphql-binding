package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import graphql.cachecontrol.CacheControl;
import graphql.execution.directives.QueryDirectives;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLEnvironment;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.GraphQLTest;

public class EnvironmentTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testFullEnvironment()
	{
		Result result = execute("{ fullEnvironment }");
		result.assertNoErrors();

		assertThat(result.pick("fullEnvironment"), is(true));
	}

	@Test
	public void testSelectionSet()
	{
		Result result = execute("{ selectionSet }");
		result.assertNoErrors();

		assertThat(result.pick("selectionSet"), is(true));
	}

	@Test
	public void testQueryDirectives()
	{
		Result result = execute("{ queryDirectives }");
		result.assertNoErrors();

		assertThat(result.pick("queryDirectives"), is(true));
	}

	@Test
	public void testCacheControl()
	{
		Result result = execute("{ cacheControl }");
		result.assertNoErrors();

		assertThat(result.pick("cacheControl"), is(true));
	}

	public class Root
	{
		@GraphQLField
		public boolean fullEnvironment(
			@GraphQLEnvironment DataFetchingEnvironment env
		)
		{
			return env != null;
		}

		@GraphQLField
		public boolean selectionSet(
			@GraphQLEnvironment DataFetchingFieldSelectionSet env
		)
		{
			return env != null;
		}

		@GraphQLField
		public boolean queryDirectives(
			@GraphQLEnvironment QueryDirectives env
		)
		{
			return env != null;
		}

		@GraphQLField
		public boolean cacheControl(
			@GraphQLEnvironment CacheControl env
		)
		{
			return env != null;
		}
	}
}
