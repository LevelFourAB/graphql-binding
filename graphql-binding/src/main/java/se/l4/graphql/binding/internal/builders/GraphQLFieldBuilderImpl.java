package se.l4.graphql.binding.internal.builders;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import se.l4.commons.types.reflect.MemberRef;
import se.l4.graphql.binding.annotations.GraphQLDeprecated;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldResult;
import se.l4.graphql.binding.resolver.output.GraphQLArgumentBuilder;
import se.l4.graphql.binding.resolver.output.GraphQLFieldBuilder;

public class GraphQLFieldBuilderImpl<Parent>
	implements GraphQLFieldBuilder<Parent>
{
	private final GraphQLResolverContext context;

	private final Parent parent;

	private final BiConsumer<GraphQLFieldDefinition, DataFetchingSupplier<?>> consumer;

	private String name;
	private String description;
	private Annotation[] annotations;

	private boolean isDeprecated;
	private String deprecationReason;

	private GraphQLOutputType type;
	private final List<GraphQLArgument> arguments;

	private DataFetchingSupplier<?> supplier;
	private Breadcrumb breadcrumb;

	public GraphQLFieldBuilderImpl(
		GraphQLResolverContext context,
		Breadcrumb breadcrumb,

		Parent parent,
		BiConsumer<GraphQLFieldDefinition, DataFetchingSupplier<?>> consumer
	)
	{
		this.context = context;

		this.breadcrumb = breadcrumb;

		this.parent = parent;

		this.consumer = consumer;

		arguments = new ArrayList<>();
	}

	@Override
	public GraphQLFieldBuilder<Parent> over(MemberRef member)
	{
		breadcrumb = breadcrumb.then(Breadcrumb.forMember(member));

		this.name = context.getMemberName(member);
		this.description = context.getDescription(member);
		this.annotations = member.getAnnotations();

		Optional<GraphQLDeprecated> deprecated = member.findAnnotation(GraphQLDeprecated.class);
		if(deprecated.isPresent())
		{
			this.setDeprecation(deprecated.get().value());
		}

		return this;
	}

	@Override
	public GraphQLFieldBuilder<Parent> setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public GraphQLFieldBuilder<Parent> setDescription(String description)
	{
		this.description = description;

		return this;
	}

	@Override
	public GraphQLFieldBuilder<Parent> setDeprecation(String reason)
	{
		this.isDeprecated = true;
		this.deprecationReason = reason;

		return this;
	}

	@Override
	public GraphQLFieldBuilder<Parent> setType(GraphQLOutputType type)
	{
		this.type = type;

		return this;
	}

	@Override
	public GraphQLArgumentBuilder<GraphQLFieldBuilder<Parent>> newArgument()
	{
		return new GraphQLArgumentBuilderImpl<>(
			context,
			breadcrumb,
			this,
			arg -> {
				for(GraphQLArgument a : arguments)
				{
					if(a.getName().equals(arg.getName()))
					{
						throw context.newError(breadcrumb, "Argument name `" + arg.getName() + "` can not be used twice");
					}
				}

				arguments.add(arg);
			}
		);
	}

	@Override
	public GraphQLFieldBuilder<Parent> withSupplier(DataFetchingSupplier<?> fetcher)
	{
		this.supplier = fetcher;

		return this;
	}

	@Override
	public Parent done()
	{
		context.breadcrumb(breadcrumb, () -> {
			if(name == null || name.trim().isEmpty())
			{
				throw context.newError("Field does not have a name");
			}

			if(type == null)
			{
				throw context.newError("Field `" + name + "` does not have a return type");
			}

			GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition()
				.name(name)
				.description(description)
				.type(type)
				.arguments(arguments);

			if(isDeprecated)
			{
				builder.deprecate(deprecationReason);
			}

			GraphQLFieldDefinition field = builder.build();

			if(annotations != null && supplier != null)
			{
				GraphQLDirectiveFieldResult result = context.applyFieldDirectives(annotations, field, supplier);
				field = result.getField();
				supplier = result.getSupplier();
			}

			consumer.accept(field, supplier);
		});

		return parent;
	}

}
