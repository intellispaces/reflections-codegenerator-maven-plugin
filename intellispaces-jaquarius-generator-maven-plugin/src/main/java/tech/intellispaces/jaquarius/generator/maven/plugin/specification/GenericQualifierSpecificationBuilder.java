package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class GenericQualifierSpecificationBuilder {
  private String alias;
  private List<DomainReference> extendedDomains = List.of();

  public GenericQualifierSpecificationBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public GenericQualifierSpecificationBuilder extendedDomains(List<DomainReference> extendedDomains) {
    this.extendedDomains = extendedDomains;
    return this;
  }

  public GenericQualifierSpecification get() {
    return new GenericQualifierSpecificationImpl(
        alias,
        extendedDomains
    );
  }
}
