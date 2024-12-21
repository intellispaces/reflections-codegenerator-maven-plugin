package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

record OntologySpecificationV0Impl(
    List<DomainSpecificationV0> domains
) implements OntologySpecificationV0 {
}
