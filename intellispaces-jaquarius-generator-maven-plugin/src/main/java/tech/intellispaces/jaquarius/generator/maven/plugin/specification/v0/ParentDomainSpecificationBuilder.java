package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public class ParentDomainSpecificationBuilder {
  private String label;
  private List<GenericQualifierDefinition> genericQualifierDefinitions;

  public ParentDomainSpecificationBuilder label(String label) {
    this.label = label;
    return this;
  }

  public ParentDomainSpecificationBuilder genericQualifierDefinitions(
      List<GenericQualifierDefinition> definitions
  ) {
    this.genericQualifierDefinitions = definitions;
    return this;
  }

  public ParentDomainSpecification get() {
    return new ParentDomainSpecificationImpl(
        label,
        genericQualifierDefinitions
    );
  }
}
