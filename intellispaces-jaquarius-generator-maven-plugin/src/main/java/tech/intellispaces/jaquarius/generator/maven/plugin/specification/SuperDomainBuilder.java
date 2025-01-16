package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class SuperDomainBuilder {
  private DomainReference domain;
  private List<ContextEquivalence> equivalences = List.of();

  public SuperDomainBuilder domain(DomainReference domain) {
    this.domain = domain;
    return this;
  }

  public SuperDomainBuilder equivalences(List<ContextEquivalence> equivalences) {
    this.equivalences = equivalences;
    return this;
  }

  public SuperDomain get() {
    return new SuperDomainImpl(
        domain,
        equivalences
    );
  }
}
