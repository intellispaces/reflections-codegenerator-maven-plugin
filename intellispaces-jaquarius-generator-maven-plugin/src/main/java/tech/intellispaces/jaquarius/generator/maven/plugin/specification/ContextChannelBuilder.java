package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class ContextChannelBuilder {
  private String alias;
  private String cid;
  private String name;
  private String description;
  private List<ContextChannel> projections = List.of();
  private DomainReference targetDomain;
  private List<ContextEquivalence> targetEquivalences = List.of();
  private String targetAlias;
  private Value targetValue;
  private List<String> allowedTraverses;

  public ContextChannelBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public ContextChannelBuilder cid(String cid) {
    this.cid = cid;
    return this;
  }

  public ContextChannelBuilder name(String name) {
    this.name = name;
    return this;
  }

  public ContextChannelBuilder description(String description) {
    this.description = description;
    return this;
  }

  public ContextChannelBuilder projections(List<ContextChannel> projections) {
    this.projections = projections;
    return this;
  }

  public ContextChannelBuilder targetDomain(DomainReference targetDomain) {
    this.targetDomain = targetDomain;
    return this;
  }

  public ContextChannelBuilder targetEquivalences(List<ContextEquivalence> targetEquivalences) {
    this.targetEquivalences = targetEquivalences;
    return this;
  }

  public ContextChannelBuilder targetAlias(String alias) {
    this.targetAlias = alias;
    return this;
  }

  public ContextChannelBuilder targetValue(Value targetValue) {
    this.targetValue = targetValue;
    return this;
  }

  public ContextChannelBuilder allowedTraverses(List<String> allowedTraverses) {
    this.allowedTraverses = allowedTraverses;
    return this;
  }

  public ContextChannel get() {
    return new ContextChannelImpl(
        alias,
        cid,
        name,
        description,
        projections,
        targetDomain,
        targetEquivalences,
        targetAlias,
        targetValue,
        allowedTraverses
    );
  }
}
