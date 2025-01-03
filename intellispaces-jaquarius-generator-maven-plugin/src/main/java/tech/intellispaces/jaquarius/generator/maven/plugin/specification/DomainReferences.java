package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public interface DomainReferences {

  static DomainReferenceBuilder build() {
    return new DomainReferenceBuilder();
  }
}
