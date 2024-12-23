package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public interface ParentDomainSpecifications {

  static ParentDomainSpecification get(String label) {
    return new ParentDomainSpecificationImpl(label);
  }
}
