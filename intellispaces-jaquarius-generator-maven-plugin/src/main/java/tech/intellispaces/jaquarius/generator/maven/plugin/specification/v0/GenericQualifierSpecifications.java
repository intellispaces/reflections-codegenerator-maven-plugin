package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public interface GenericQualifierSpecifications {

  static GenericQualifierSpecification get(String name) {
    return new GenericQualifierSpecificationImpl(name);
  }
}
