package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.DomainSpecification;

import java.util.ArrayList;
import java.util.List;

public class UnitedSpecificationProvider implements SpecificationProvider {
  private final List<SpecificationProvider> specificationProviders = new ArrayList<>();

  public void addProvider(SpecificationProvider specificationProvider) {
    this.specificationProviders.add(specificationProvider);
  }

  @Override
  public DomainSpecification domainV0ByName(String domainName) {
    for (SpecificationProvider sp : specificationProviders) {
      DomainSpecification domainSpecification = sp.domainV0ByName(domainName);
      if (domainSpecification != null) {
        return domainSpecification;
      }
    }
    return null;
  }
}
