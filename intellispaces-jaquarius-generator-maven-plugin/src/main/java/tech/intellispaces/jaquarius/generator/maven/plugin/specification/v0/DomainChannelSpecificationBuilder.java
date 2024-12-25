package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public class DomainChannelSpecificationBuilder {
  private String targetDomainName;
  private String alias;
  private String cid;
  private String name;
  private List<String> allowedTraverses;
  private List<ChannelQualifiedSpecification> qualifiers = List.of();

  public DomainChannelSpecificationBuilder targetDomainName(String targetDomainName) {
    this.targetDomainName = targetDomainName;
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

  public DomainChannelSpecificationBuilder qualifiers(List<ChannelQualifiedSpecification> qualifiers) {
    this.qualifiers = qualifiers;
    return this;
  }

  public DomainChannelSpecification get() {
    return new DomainChannelSpecificationImpl(
        targetDomainName,
        alias,
        cid,
        name,
        allowedTraverses,
        qualifiers
    );
  }
}
