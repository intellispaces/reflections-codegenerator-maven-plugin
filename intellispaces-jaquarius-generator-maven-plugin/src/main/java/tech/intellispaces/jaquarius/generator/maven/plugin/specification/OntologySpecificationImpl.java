package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record OntologySpecificationImpl(
    List<DomainSpecification> domains
) implements OntologySpecification {
}
