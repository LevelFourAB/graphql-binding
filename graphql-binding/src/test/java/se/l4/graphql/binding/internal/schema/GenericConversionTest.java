package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;
import se.l4.graphql.binding.resolver.GraphQLConversion;

public class GenericConversionTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withResolver(new Converter<>())
			.withResolver(new ConverterNonNull<>());
	}

	@Test
	public void testStringSchema()
	{
		GraphQLOutputType inferOutputType = schema.getQueryType().getFieldDefinition("simpleString").getType();
		assertThat(inferOutputType, is(GraphQLList.list(Scalars.GraphQLString)));
	}

	@Test
	public void testStringConversion()
	{
		Result r = execute("{ simpleNonNullString }");
		r.assertNoErrors();

		assertThat(r.pick("simpleNonNullString", "0"), is("Hello"));
	}

	@Test
	public void testNonNullStringSchema()
	{
		GraphQLOutputType inferOutputType = schema.getQueryType().getFieldDefinition("simpleNonNullString").getType();
		assertThat(inferOutputType, is(GraphQLList.list(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString))));
	}

	@Test
	public void testNonNullStringConversion()
	{
		Result r = execute("{ simpleNonNullString }");
		r.assertNoErrors();

		assertThat(r.pick("simpleNonNullString", "0"), is("Hello"));
	}

	@Test
	public void testEnforcedNonNullStringSchema()
	{
		GraphQLOutputType inferOutputType = schema.getQueryType().getFieldDefinition("enforcedNonNullString").getType();
		assertThat(inferOutputType, is(GraphQLList.list(graphql.schema.GraphQLNonNull.nonNull(Scalars.GraphQLString))));
	}

	@Test
	public void testEnforcedNonNullStringConversion()
	{
		Result r = execute("{ enforcedNonNullString }");
		r.assertNoErrors();

		assertThat(r.pick("enforcedNonNullString", "0"), is("Hello"));
	}

	@Test
	public void testFactoryConstructorStringSchema()
	{
		GraphQLOutputType inferOutputType = schema.getQueryType().getFieldDefinition("factoryConstructorString").getType();
		assertThat(((GraphQLNamedType) inferOutputType).getName(), is("StringGenericViaFactoryConstructor"));
	}

	@Test
	public void testFactoryConstructorStringConversion()
	{
		Result r = execute("{ factoryConstructorString { item } }");
		r.assertNoErrors();

		assertThat(r.pick("factoryConstructorString", "item"), is("Hello"));
	}

	@Test
	public void testFactoryConstructorWithSimpleStringSchema()
	{
		GraphQLOutputType inferOutputType = schema.getQueryType().getFieldDefinition("factoryConstructorWithSimpleInteger").getType();
		assertThat(((GraphQLNamedType) inferOutputType).getName(), is("IntListGenericViaFactoryConstructor"));
	}

	@Test
	public void testFactoryConstructorWithSimpleStringConversion()
	{
		Result r = execute("{ factoryConstructorWithSimpleInteger { item } }");
		r.assertNoErrors();

		assertThat(r.pick("factoryConstructorWithSimpleInteger", "item", "0"), is(10));
	}

	public class Root
	{
		@GraphQLField
		public Generic<String> simpleString()
		{
			return new Generic<>("Hello");
		}

		@GraphQLField
		public Generic<@GraphQLNonNull String> simpleNonNullString()
		{
			return new Generic<>("Hello");
		}

		@GraphQLField
		public GenericNonNull<String> enforcedNonNullString()
		{
			return new GenericNonNull<>("Hello");
		}

		@GraphQLField
		public GenericViaFactoryConstructor<String> factoryConstructorString()
		{
			return new GenericViaFactoryConstructor<>("Hello");
		}

		@GraphQLField
		public GenericViaFactoryConstructor<Generic<Integer>> factoryConstructorWithSimpleInteger()
		{
			return new GenericViaFactoryConstructor<>(new Generic<>(10));
		}
	}

	private class Generic<T>
	{
		private final T item;

		public Generic(T item)
		{
			this.item = item;
		}
	}

	private class Converter<T>
		implements GraphQLConversion<Generic<T>, List<T>>
	{
		@Override
		public List<T> convert(DataFetchingEnvironment environment, Generic<T> object)
		{
			return ImmutableList.of(object.item);
		}
	}

	private class GenericNonNull<T>
	{
		private final T item;

		public GenericNonNull(T item)
		{
			this.item = item;
		}
	}

	private class ConverterNonNull<T>
		implements GraphQLConversion<GenericNonNull<T>, List<@GraphQLNonNull T>>
	{
		@Override
		public List<T> convert(DataFetchingEnvironment environment, GenericNonNull<T> object)
		{
			return ImmutableList.of(object.item);
		}
	}

	@GraphQLObject
	public class GenericViaFactoryConstructor<T>
	{
		private final T item;

		@GraphQLFactory
		public GenericViaFactoryConstructor(@GraphQLSource T item)
		{
			this.item = item;
		}

		@GraphQLField
		public T item()
		{
			return item;
		}
	}
}
