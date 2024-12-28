package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public class DomainChannelSpecificationBuilder {
  private String targetDomainName;
  private String targetDomainRef;
  private String targetValueRef;
  private String alias;
  private String cid;
  private String name;
  private List<String> allowedTraverses;
  private List<ValueQualifiedSpecification> valueQualifiers = List.of();

  public DomainChannelSpecificationBuilder targetDomainName(String targetDomainName) {
    this.targetDomainName = targetDomainName;
    return this;
  }

  public DomainChannelSpecificationBuilder targetDomainRef(String targetDomainRef) {
    this.targetDomainRef = targetDomainRef;
    return this;
  }

  public DomainChannelSpecificationBuilder targetValueRef(String targetValueRef) {
    this.targetValueRef = targetValueRef;
    return this;
  }

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

  public DomainChannelSpecificationBuilder allowedTraverses(List<String> allowedTraverses) {
    this.allowedTraverses = allowedTraverses;
    return this;
  }

  public DomainChannelSpecificationBuilder valueQualifiers(List<ValueQualifiedSpecification> valueQualifiers) {
    this.valueQualifiers = valueQualifiers;
    return this;
  }

  public DomainChannelSpecification get() {
    return new DomainChannelSpecificationImpl(
        targetDomainName,
        targetDomainRef,
        targetValueRef,
        alias,
        cid,
        name,
        allowedTraverses,
        valueQualifiers
    );
  }
}
