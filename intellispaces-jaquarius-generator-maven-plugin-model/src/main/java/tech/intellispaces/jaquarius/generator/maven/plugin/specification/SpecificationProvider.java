package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.DomainSpecification;

public interface SpecificationProvider {

  DomainSpecification domainV0ByName(String domainName);
}
