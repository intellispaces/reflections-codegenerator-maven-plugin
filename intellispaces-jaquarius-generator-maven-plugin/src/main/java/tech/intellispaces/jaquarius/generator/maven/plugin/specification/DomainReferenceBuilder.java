package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class DomainReferenceBuilder {
  private String name;
  private List<DomainReference> superDomainBounds;

  public DomainReferenceBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DomainReferenceBuilder superDomainBounds(List<DomainReference> superDomainBounds) {
    this.superDomainBounds = superDomainBounds;
    return this;
  }

  public DomainReference get() {
    return new DomainReferenceImpl(
        name,
        superDomainBounds
    );
  }
}
