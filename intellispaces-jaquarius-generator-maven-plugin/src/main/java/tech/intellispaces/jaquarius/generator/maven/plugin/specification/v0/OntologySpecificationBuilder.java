package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.ArrayList;
import java.util.List;

public class OntologySpecificationBuilder {
  private List<DomainSpecification> domains = new ArrayList<>();

  public OntologySpecificationBuilder domains(List<DomainSpecification> domains) {
    this.domains = domains;
    return this;
  }

  public OntologySpecification get() {
    return new OntologySpecificationImpl(
        domains
    );
  }
}
