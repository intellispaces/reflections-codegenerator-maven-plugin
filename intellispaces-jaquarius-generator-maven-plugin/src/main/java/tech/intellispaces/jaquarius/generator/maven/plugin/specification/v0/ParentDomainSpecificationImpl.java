package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

record ParentDomainSpecificationImpl(
    String label,
    List<GenericQualifierDefinition> genericQualifierDefinitions
) implements ParentDomainSpecification {
}
