package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public interface DomainSpecifications {

  static DomainSpecificationBuilder build() {
    return new DomainSpecificationBuilder();
  }
}
