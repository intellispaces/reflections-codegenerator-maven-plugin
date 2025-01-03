package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.nio.file.Path;

public class SpecificationBuilder {
  private final Path specPath;
  private OntologySpecification ontology;

  public SpecificationBuilder(Path specPath) {
    this.specPath = specPath;
  }

  public SpecificationBuilder ontology(OntologySpecification ontology) {
    this.ontology = ontology;
    return this;
  }

  public Specification get() {
    return new SpecificationImpl(
        specPath,
        ontology
    );
  }
}
