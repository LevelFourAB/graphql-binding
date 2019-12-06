package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLFactory;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.annotations.GraphQLName;
import se.l4.graphql.binding.annotations.GraphQLNonNull;
import se.l4.graphql.binding.annotations.GraphQLObject;
import se.l4.graphql.binding.annotations.GraphQLSource;
import se.l4.graphql.binding.internal.GraphQLTest;

public class SelfReferencingTypeTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root())
			.withType(ConvertingDirectRef.class);
	}

	@Test
	public void testDirectNonNullSchema()
	{
		GraphQLObjectType type = schema.getObjectType("DirectRef");
		GraphQLFieldDefinition def =  type.getFieldDefinition("andNonNull");
		assertThat(def.getType(), instanceOf(graphql.schema.GraphQLNonNull.class));
	}

	@Test
	public void testDirectRoot()
	{
		Result result = execute("{ direct(name: \"Hello\") { name } }");
		result.assertNoErrors();

		assertThat(result.pick("direct", "name"), is("Hello"));
	}

	@Test
	public void testDirectReference()
	{
		Result result = execute("{ direct(name: \"Hello\") { name, and(other: \"World\") { name } } }");
		result.assertNoErrors();

		assertThat(result.pick("direct", "name"), is("Hello"));
		assertThat(result.pick("direct", "and", "name"), is("Hello World"));
	}

	@Test
	public void testIndirectSchema()
	{
		GraphQLObjectType type = (GraphQLObjectType) schema.getType("IndirectRef");
		GraphQLOutputType childrenType = type.getFieldDefinition("children")
			.getType();

		assertThat(childrenType, is(instanceOf(GraphQLList.class)));
	}

	@Test
	public void testIndirectNonNullSchema()
	{
		GraphQLObjectType type = schema.getObjectType("IndirectRef");
		GraphQLFieldDefinition def =  type.getFieldDefinition("childrenNonNull");
		GraphQLList listType = (GraphQLList) def.getType();
		assertThat(listType.getChildren().get(0), instanceOf(graphql.schema.GraphQLNonNull.class));
	}

	@Test
	public void testIndirectRoot()
	{
		Result result = execute("{ indirect(name: \"Hello\") { name } }");
		result.assertNoErrors();

		assertThat(result.pick("indirect", "name"), is("Hello"));
	}

	@Test
	public void testIndirectReference()
	{
		Result result = execute("{ indirect(name: \"Hello\") { name, children { name } } }");
		result.assertNoErrors();

		assertThat(result.pick("indirect", "name"), is("Hello"));
		assertThat(result.pick("indirect", "children", "0", "name"), is("Hello 0"));
		assertThat(result.pick("indirect", "children", "1", "name"), is("Hello 1"));
	}

	@Test
	public void testConvertingDirectRoot()
	{
		Result result = execute("{ convertingDirect(name: \"Hello\") { name } }");
		result.assertNoErrors();

		assertThat(result.pick("convertingDirect", "name"), is("Hello"));
	}

	@Test
	public void testConvertingDirectReference()
	{
		Result result = execute("{ convertingDirect(name: \"Hello\") { name, and(other: \"World\") { name } } }");
		result.assertNoErrors();

		assertThat(result.pick("convertingDirect", "name"), is("Hello"));
		assertThat(result.pick("convertingDirect", "and", "name"), is("Hello World"));
	}

	@Test
	public void testDirectInput()
	{
		Result result = execute("{ directInput(item: { name: \"Hello\", sub: { name: \" World\" } }) }");
		result.assertNoErrors();

		assertThat(result.pick("directInput"), is("Hello World"));
	}

	public class Root
	{
		@GraphQLField
		public DirectRef direct(
			@GraphQLName("name") String name
		)
		{
			return new DirectRef(name);
		}

		@GraphQLField
		public IndirectRef indirect(
			@GraphQLName("name") String name
		)
		{
			return new IndirectRef(name);
		}

		@GraphQLField
		public NameHolder convertingDirect(
			@GraphQLName("name") String name
		)
		{
			return new NameHolder(name);
		}

		@GraphQLField
		public String directInput(
			@GraphQLName("item") DirectRefInput in
		)
		{
			return resolve(in);
		}

		private String resolve(DirectRefInput in)
		{
			if(in == null) return "";

			return in.name + resolve(in.sub);
		}
	}

	@GraphQLObject
	public class DirectRef
	{
		private final String name;

		public DirectRef(String name)
		{
			this.name = name;
		}

		@GraphQLField
		@GraphQLNonNull
		public String name()
		{
			return name;
		}

		@GraphQLField
		public DirectRef and(
			@GraphQLNonNull @GraphQLName("other") String name
		)
		{
			return new DirectRef(this.name + " " + name);
		}

		@GraphQLField
		@GraphQLNonNull
		public DirectRef andNonNull(
			@GraphQLNonNull @GraphQLName("other") String name
		)
		{
			return new DirectRef(this.name + " " + name);
		}
	}

	@GraphQLObject
	public class IndirectRef
	{
		private final String name;

		public IndirectRef(String name)
		{
			this.name = name;
		}

		@GraphQLField
		public String name()
		{
			return name;
		}

		@GraphQLField
		public List<IndirectRef> children()
		{
			return ImmutableList.of(
				new IndirectRef(this.name + " 0"),
				new IndirectRef(this.name + " 1")
			);
		}

		@GraphQLField
		public List<@GraphQLNonNull IndirectRef> childrenNonNull()
		{
			return ImmutableList.of(
				new IndirectRef(this.name + " 0"),
				new IndirectRef(this.name + " 1")
			);
		}
	}

	public class NameHolder
	{
		private final String name;

		public NameHolder(String name)
		{
			this.name = name;
		}
	}

	@GraphQLObject
	public class ConvertingDirectRef
	{
		private final NameHolder holder;

		@GraphQLFactory
		public ConvertingDirectRef(@GraphQLSource NameHolder holder)
		{
			this.holder = holder;
		}

		@GraphQLField
		public String name()
		{
			return holder.name;
		}

		@GraphQLField
		public NameHolder and(
			@GraphQLNonNull @GraphQLName("other") String name
		)
		{
			return new NameHolder(holder.name + " " + name);
		}
	}

	@GraphQLInputObject
	public static class DirectRefInput
	{
		@GraphQLField
		public String name;

		@GraphQLField
		public DirectRefInput sub;
	}
}
