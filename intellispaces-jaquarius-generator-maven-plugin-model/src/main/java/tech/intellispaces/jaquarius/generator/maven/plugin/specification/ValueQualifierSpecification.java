package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public interface ValueQualifierSpecification {

  /**
   * The qualifier name.
   */
  String name();

  DomainReference domain();
}
