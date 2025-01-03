package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record GenericQualifierSpecificationImpl(
    String alias,
    List<DomainReference> extendedDomains
) implements GenericQualifierSpecification {
}
