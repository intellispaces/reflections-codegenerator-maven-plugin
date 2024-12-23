package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersion;

record SpecificationImpl(
    SpecificationVersion version,
    OntologySpecification ontology
) implements SpecificationV0 {
}
