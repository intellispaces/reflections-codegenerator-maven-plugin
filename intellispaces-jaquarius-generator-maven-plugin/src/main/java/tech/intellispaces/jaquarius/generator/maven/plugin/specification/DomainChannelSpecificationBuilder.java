package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class DomainChannelSpecificationBuilder {
  private String alias;
  private String cid;
  private String name;
  private DomainReference targetDomain;
  private ValueReference targetValue;
  private List<ValueQualifierSpecification> valueQualifiers = List.of();
  private List<String> allowedTraverses;

  public DomainChannelSpecificationBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public DomainChannelSpecificationBuilder cid(String cid) {
    this.cid = cid;
    return this;
  }

  public DomainChannelSpecificationBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DomainChannelSpecificationBuilder targetDomain(DomainReference targetDomain) {
    this.targetDomain = targetDomain;
    return this;
  }

  public DomainChannelSpecificationBuilder targetValue(ValueReference targetValue) {
    this.targetValue = targetValue;
    return this;
  }

  public DomainChannelSpecificationBuilder valueQualifiers(List<ValueQualifierSpecification> valueQualifiers) {
    this.valueQualifiers = valueQualifiers;
    return this;
  }

  public DomainChannelSpecificationBuilder allowedTraverses(List<String> allowedTraverses) {
    this.allowedTraverses = allowedTraverses;
    return this;
  }

  public DomainChannelSpecification get() {
    return new DomainChannelSpecificationImpl(
        alias,
        cid,
        name,
        targetDomain,
        targetValue,
        valueQualifiers,
        allowedTraverses
    );
  }
}
