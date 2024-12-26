package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersion;

import java.nio.file.Path;

record SpecificationImpl(
    Path path,
    SpecificationVersion version,
    OntologySpecification ontology
) implements SpecificationV0 {
}
