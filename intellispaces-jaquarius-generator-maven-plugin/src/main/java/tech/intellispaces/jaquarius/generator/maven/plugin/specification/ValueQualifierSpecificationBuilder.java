package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public class ValueQualifierSpecificationBuilder {
  private String name;
  private DomainReference domain;

  public ValueQualifierSpecificationBuilder name(String name) {
    this.name = name;
    return this;
  }

  public ValueQualifierSpecificationBuilder domain(DomainReference domain) {
    this.domain = domain;
    return this;
  }

  public ValueQualifierSpecification get() {
    return new ValueQualifierSpecificationImpl(
        name,
        domain
    );
  }
}
