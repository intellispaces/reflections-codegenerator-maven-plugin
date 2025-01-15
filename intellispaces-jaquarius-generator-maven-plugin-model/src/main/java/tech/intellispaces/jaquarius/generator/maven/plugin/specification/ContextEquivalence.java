package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

/**
 * The context equivalence specification.
 */
public interface ContextEquivalence {

  /**
   * The context object projection alias.
   */
  String projectionAlias();

  /**
   * The matched projection.
   */
  String matchedProjectionAlias();
}
