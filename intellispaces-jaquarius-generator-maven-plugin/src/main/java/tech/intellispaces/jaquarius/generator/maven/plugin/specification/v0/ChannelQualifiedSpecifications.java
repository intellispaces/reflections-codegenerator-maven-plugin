package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public interface ChannelQualifiedSpecifications {

  static ChannelQualifiedSpecification get(String name, String domainName) {
    return new ChannelQualifiedSpecificationImpl(name, domainName);
  }
}
