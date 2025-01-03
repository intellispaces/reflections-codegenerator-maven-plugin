package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record DomainReferenceImpl(
    String name,
    String alias,
    List<GenericQualifierAppointment> genericQualifiers
) implements DomainReference {
}
