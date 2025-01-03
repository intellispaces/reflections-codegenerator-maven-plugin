package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public interface DomainChannelSpecifications {

  static DomainChannelSpecificationBuilder build() {
    return new DomainChannelSpecificationBuilder();
  }
}
