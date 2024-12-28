package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public interface ValueQualifiedSpecifications {

  static ValueQualifiedSpecification get(String name, String domainName) {
    return new ValueQualifiedSpecificationImpl(name, domainName);
  }
}
