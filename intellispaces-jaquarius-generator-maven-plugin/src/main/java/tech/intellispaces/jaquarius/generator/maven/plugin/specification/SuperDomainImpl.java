package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record SuperDomainImpl(
    DomainReference domain,
    List<ContextEquivalence> equivalences
) implements SuperDomain {
}
