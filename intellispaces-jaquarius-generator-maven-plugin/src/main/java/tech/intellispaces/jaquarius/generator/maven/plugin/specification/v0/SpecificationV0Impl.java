package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersion;

record SpecificationV0Impl(
    SpecificationVersion version,
    OntologySpecificationV0 ontology
) implements SpecificationV0 {
}
