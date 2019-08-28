package se.l4.graphql.binding.internal.resolvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import se.l4.commons.types.reflect.FieldRef;
import se.l4.commons.types.reflect.TypeRef;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.internal.DataFetchingConversion;
import se.l4.graphql.binding.internal.datafetchers.FieldInjector;
import se.l4.graphql.binding.internal.datafetchers.ValueInjector;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.ResolverContext;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;

public class InputObjectTypeResolver
	implements GraphQLInputResolver
{

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputType> resolveInput(GraphQLInputEncounter encounter)
	{
		ResolverContext context = encounter.getContext();
		TypeRef type = encounter.getType();

		GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
			.name(context.getTypeName(type))
			.description(context.getDescription(type));

		List<ValueInjector> injectors = new ArrayList<>();

		// Go through all the Java fields in the type and map them
		for(FieldRef field : type.getDeclaredFields())
		{
			if(! field.findAnnotation(GraphQLField.class).isPresent())
			{
				continue;
			}

			context.breadcrumb(Breadcrumb.forMember(field), () -> {
				if(! field.isPublic())
				{
					throw context.newError(
						"Field must be public to be useable"
					);
				};

				if(field.isFinal())
				{
					throw context.newError(
						"Field can not be final"
					);
				}

				ResolvedGraphQLType<? extends GraphQLInputType> fieldType = context.resolveInput(field.getType());
				String name = context.getMemberName(field);

				injectors.add(new FieldInjector(field.getField(), name, fieldType.getConversion()));

				builder.field(
					GraphQLInputObjectField.newInputObjectField()
						.name(name)
						.description(context.getDescription(type))
						.type(fieldType.getGraphQLType())
				);
			});
		}

		Supplier<Object> supplier = context.getInstanceFactory().supplier(type.getType());
		InputObjectFactory factory = new InputObjectFactory(
			(env, source) -> supplier.get(),
			injectors.toArray(ValueInjector[]::new)
		);

		return ResolvedGraphQLType.forType(builder.build())
			.withConversion(factory);
	}

	private static class InputObjectFactory
		implements DataFetchingConversion<Object, Object>
	{
		private final DataFetchingConversion<Object, Object> instanceFactory;
		private final ValueInjector[] valueInjectors;

		public InputObjectFactory(
			DataFetchingConversion<Object, Object> instanceFactory,
			ValueInjector[] valueInjectors
		)
		{
			this.instanceFactory = instanceFactory;
			this.valueInjectors = valueInjectors;
		}

		@Override
		public Object convert(DataFetchingEnvironment environment, Object object)
		{
			Object instance = instanceFactory.convert(environment, object);
			Map<String, Object> data = (Map) object;

			for(ValueInjector injector : valueInjectors)
			{
				injector.inject(environment, instance, data);
			}

			return instance;
		}
	}
}
