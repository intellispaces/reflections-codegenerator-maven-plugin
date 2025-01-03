package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public interface DomainSpecifications {

  static DomainSpecificationBuilder build() {
    return new DomainSpecificationBuilder();
  }
}
