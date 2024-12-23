package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

record OntologySpecificationImpl(
    List<DomainSpecification> domains
) implements OntologySpecification {
}
