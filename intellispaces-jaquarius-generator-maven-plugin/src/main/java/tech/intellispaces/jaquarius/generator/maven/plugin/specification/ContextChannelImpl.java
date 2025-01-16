package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record ContextChannelImpl(
    String alias,
    String cid,
    String name,
    String description,
    List<ContextChannel> projections,
    DomainReference targetDomain,
    List<ContextEquivalence> targetEquivalences,
    String targetAlias,
    List<String> allowedTraverse
) implements ContextChannel {
}
