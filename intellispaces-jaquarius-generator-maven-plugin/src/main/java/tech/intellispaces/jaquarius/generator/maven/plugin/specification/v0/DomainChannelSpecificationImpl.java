package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

record DomainChannelSpecificationImpl(
    String targetDomainName,
    String alias,
    String cid,
    String name,
    List<String> allowedTraverse,
    List<ChannelQualifiedSpecification> qualifiers
) implements DomainChannelSpecification {
}
