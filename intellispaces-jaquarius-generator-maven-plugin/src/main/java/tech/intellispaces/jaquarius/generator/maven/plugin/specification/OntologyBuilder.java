package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.ArrayList;
import java.util.List;

public class OntologyBuilder {
  private List<Domain> domains = new ArrayList<>();

  public OntologyBuilder domains(List<Domain> domains) {
    this.domains = domains;
    return this;
  }

  public Ontology get() {
    return new OntologyImpl(
        domains
    );
  }
}
