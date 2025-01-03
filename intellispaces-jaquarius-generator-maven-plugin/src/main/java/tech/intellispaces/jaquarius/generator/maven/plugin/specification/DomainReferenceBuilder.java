package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class DomainReferenceBuilder {
  private String name;
  private String alias;
  private List<GenericQualifierAppointment> genericQualifiers = List.of();

  public DomainReferenceBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DomainReferenceBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public DomainReferenceBuilder genericQualifiers(List<GenericQualifierAppointment> qualifiers) {
    this.genericQualifiers = qualifiers;
    return this;
  }

  public DomainReference get() {
    return new DomainReferenceImpl(
        name,
        alias,
        genericQualifiers
    );
  }
}
