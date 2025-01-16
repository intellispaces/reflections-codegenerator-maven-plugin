package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record DomainImpl(
    String name,
    String did,
    String description,
    List<SuperDomain> superDomains,
    List<ContextChannel> channels
) implements Domain {
}
