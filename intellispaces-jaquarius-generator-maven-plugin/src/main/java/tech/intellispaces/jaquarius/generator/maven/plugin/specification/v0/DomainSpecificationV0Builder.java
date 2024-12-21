package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public class DomainSpecificationV0Builder {
  private String label;
  private String did;
  private String description;

  public DomainSpecificationV0Builder label(String label) {
    this.label = label;
    return this;
  }

  public DomainSpecificationV0Builder did(String did) {
    this.did = did;
    return this;
  }

  public DomainSpecificationV0Builder description(String description) {
    this.description = description;
    return this;
  }

  public DomainSpecificationV0 get() {
    return new DomainSpecificationV0Impl(
        label,
        did,
        description
    );
  }
}
