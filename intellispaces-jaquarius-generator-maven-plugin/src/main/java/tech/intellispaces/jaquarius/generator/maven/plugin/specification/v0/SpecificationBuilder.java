package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersion;

import java.nio.file.Path;

public class SpecificationBuilder {
  private final Path path;
  private SpecificationVersion version;
  private OntologySpecification ontology;

  public SpecificationBuilder(Path path) {
    this.path = path;
  }

  public SpecificationBuilder version(SpecificationVersion version) {
    this.version = version;
    return this;
  }

  public SpecificationBuilder ontology(OntologySpecification ontology) {
    this.ontology = ontology;
    return this;
  }

  public SpecificationV0 get() {
    return new SpecificationImpl(
        path,
        version,
        ontology
    );
  }
}
