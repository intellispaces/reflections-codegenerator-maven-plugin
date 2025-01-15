package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

record ContextEquivalenceImpl(
    String projectionAlias,
    String matchedProjectionAlias
) implements ContextEquivalence {
}
