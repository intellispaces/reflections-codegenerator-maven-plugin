package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public class DomainSpecificationBuilder {
  private String label;
  private String did;
  private String description;
  private List<ParentDomainSpecification> parents = List.of();

  public DomainSpecificationBuilder label(String label) {
    this.label = label;
    return this;
  }

  public DomainSpecificationBuilder did(String did) {
    this.did = did;
    return this;
  }

  public DomainSpecificationBuilder description(String description) {
    this.description = description;
    return this;
  }

  public DomainSpecificationBuilder parents(List<ParentDomainSpecification> parents) {
    this.parents = parents;
    return this;
  }

  public DomainSpecification get() {
    return new DomainSpecificationImpl(
        label,
        did,
        description,
        parents
    );
  }
}
