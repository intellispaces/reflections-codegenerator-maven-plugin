package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.nio.file.Path;

record SpecificationImpl(
    Path specPath,
    OntologySpecification ontology
) implements Specification {
}
