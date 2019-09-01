package se.l4.graphql.binding.internal.directive;

import java.util.function.Consumer;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldDefinition.Builder;
import se.l4.graphql.binding.resolver.DataFetchingSupplier;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.directive.GraphQLDirectiveFieldEncounter;

/**
 * Implementation of {@link GraphQLDirectiveFieldEncounter}.
 */
public class GraphQLDirectiveFieldEncounterImpl<A>
	extends GraphQLDirectiveApplyEncounterImpl<A>
	implements GraphQLDirectiveFieldEncounter<A>
{
	private GraphQLFieldDefinition field;
	private DataFetchingSupplier<?> supplier;

	public GraphQLDirectiveFieldEncounterImpl(
		GraphQLResolverContext context,
		GraphQLDirective directive,
		A annotation,

		GraphQLFieldDefinition field,
		DataFetchingSupplier<?> supplier
	)
	{
		super(context, directive, annotation);

		this.field = field;
		this.supplier = supplier;
	}

	@Override
	public GraphQLFieldDefinition getField()
	{
		return field;
	}

	@Override
	public void transformField(Consumer<Builder> consumer)
	{
		field = field.transform(consumer);
	}

	@Override
	public DataFetchingSupplier<?> getSupplier()
	{
		return supplier;
	}

	@Override
	public void setSupplier(DataFetchingSupplier<?> supplier)
	{
		this.supplier = supplier;
	}
}
