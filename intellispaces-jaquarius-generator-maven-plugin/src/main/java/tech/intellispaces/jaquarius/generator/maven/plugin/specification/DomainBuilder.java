package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class DomainBuilder {
  private String name;
  private String did;
  private String description;
  private List<DomainReference> superDomains = List.of();
  private List<ContextChannel> channels;

  public DomainBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DomainBuilder did(String did) {
    this.did = did;
    return this;
  }

  public DomainBuilder description(String description) {
    this.description = description;
    return this;
  }

  public DomainBuilder superDomains(List<DomainReference> domainReferences) {
    this.superDomains = domainReferences;
    return this;
  }

  public DomainBuilder channels(List<ContextChannel> channels) {
    this.channels = channels;
    return this;
  }

  public Domain get() {
    return new DomainImpl(
        name,
        did,
        description,
        superDomains,
        channels
    );
  }
}
