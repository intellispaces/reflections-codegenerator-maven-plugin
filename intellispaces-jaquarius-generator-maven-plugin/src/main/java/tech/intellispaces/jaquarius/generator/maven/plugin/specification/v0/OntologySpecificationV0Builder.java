package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.ArrayList;
import java.util.List;

public class OntologySpecificationV0Builder {
  private List<DomainSpecificationV0> domains = new ArrayList<>();

  public OntologySpecificationV0Builder domains(List<DomainSpecificationV0> domains) {
    this.domains = domains;
    return this;
  }

  public OntologySpecificationV0 get() {
    return new OntologySpecificationV0Impl(
        domains
    );
  }
}
