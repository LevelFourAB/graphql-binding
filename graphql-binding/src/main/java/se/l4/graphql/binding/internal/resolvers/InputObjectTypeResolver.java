package se.l4.graphql.binding.internal.resolvers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLInputObject;
import se.l4.graphql.binding.internal.datafetchers.FieldInjector;
import se.l4.graphql.binding.internal.datafetchers.ValueInjector;
import se.l4.graphql.binding.internal.factory.MemberKey;
import se.l4.graphql.binding.resolver.Breadcrumb;
import se.l4.graphql.binding.resolver.DataFetchingConversion;
import se.l4.graphql.binding.resolver.GraphQLResolverContext;
import se.l4.graphql.binding.resolver.ResolvedGraphQLType;
import se.l4.graphql.binding.resolver.input.GraphQLInputEncounter;
import se.l4.graphql.binding.resolver.input.GraphQLInputResolver;
import se.l4.ylem.types.reflect.FieldRef;
import se.l4.ylem.types.reflect.TypeRef;

public class InputObjectTypeResolver
	implements GraphQLInputResolver
{
	@Override
	public boolean supportsInput(TypeRef type)
	{
		return type.hasAnnotation(GraphQLInputObject.class);
	}

	@Override
	public ResolvedGraphQLType<? extends GraphQLInputObjectType> resolveInput(GraphQLInputEncounter encounter)
	{
		TypeRef initialType = encounter.getType();

		GraphQLResolverContext context = encounter.getContext();

		GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
			.name(context.requestInputTypeName(initialType))
			.description(context.getDescription(initialType));

		List<ValueInjector> injectors = new ArrayList<>();

		Set<MemberKey> handled = new HashSet<>();

		initialType.visitHierarchy(type -> {
			// Go through all the Java fields in the type and map them
			for(FieldRef field : type.getDeclaredFields())
			{
				if(! field.findAnnotation(GraphQLField.class).isPresent())
				{
					continue;
				}

				// Check if this is already handled
				if(! handled.add(MemberKey.create(field))) continue;

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
							.description(context.getDescription(field))
							.type(fieldType.getGraphQLType())
					);
				});
			}

			return true;
		});

		Supplier<Object> supplier = context.getInstanceFactory()
			.supplier(initialType);

		InputObjectFactory factory = new InputObjectFactory(
			(env, source) -> supplier.get(),
			injectors.toArray(new ValueInjector[injectors.size()])
		);

		return ResolvedGraphQLType.forType(builder.build())
			.withInputConversion(factory);
	}

	@Override
	public String toString()
	{
		return "@" + GraphQLInputObject.class.getSimpleName();
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
		@SuppressWarnings({ "unchecked", "rawtypes" })
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
