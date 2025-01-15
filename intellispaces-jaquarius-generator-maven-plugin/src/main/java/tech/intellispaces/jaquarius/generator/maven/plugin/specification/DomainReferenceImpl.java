package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record DomainReferenceImpl(
    String name,
    List<ContextEquivalence> equivalences,
    List<DomainReference> superDomainBounds
) implements DomainReference {
}
