package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersion;

public class SpecificationV0Builder {
  private SpecificationVersion version;
  private OntologySpecificationV0 ontology;

  public SpecificationV0Builder version(SpecificationVersion version) {
    this.version = version;
    return this;
  }

  public SpecificationV0Builder ontology(OntologySpecificationV0 ontology) {
    this.ontology = ontology;
    return this;
  }

  public SpecificationV0 get() {
    return new SpecificationV0Impl(
        version,
        ontology
    );
  }
}
