package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public class ContextEquivalenceBuilder {
  private String projectionAlias;
  private String matchedProjectionAlias;

  public ContextEquivalenceBuilder projectionAlias(String projectionAlias) {
    this.projectionAlias = projectionAlias;
    return this;
  }

  public ContextEquivalenceBuilder matchedProjectionAlias(String matchedProjectionAlias) {
    this.matchedProjectionAlias = matchedProjectionAlias;
    return this;
  }

  public ContextEquivalence get() {
    return new ContextEquivalenceImpl(projectionAlias, matchedProjectionAlias);
  }
}
