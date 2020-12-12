module se.l4.graphql.binding {
	requires com.github.spotbugs.annotations;

	requires transitive com.graphqljava;
	requires org.eclipse.collections.api;
	requires se.l4.ylem.types.matching;
	requires org.reactivestreams;

	requires transitive se.l4.ylem.types.conversion;
	requires transitive se.l4.ylem.types.discovery;
	requires transitive se.l4.ylem.types.instances;
	requires transitive se.l4.ylem.types.reflect;

	exports se.l4.graphql.binding;
	exports se.l4.graphql.binding.annotations;
	exports se.l4.graphql.binding.naming;
	exports se.l4.graphql.binding.resolver;
	exports se.l4.graphql.binding.resolver.directive;
	exports se.l4.graphql.binding.resolver.input;
	exports se.l4.graphql.binding.resolver.output;

	opens se.l4.graphql.binding.internal to se.l4.ylem.types.instances;
}
