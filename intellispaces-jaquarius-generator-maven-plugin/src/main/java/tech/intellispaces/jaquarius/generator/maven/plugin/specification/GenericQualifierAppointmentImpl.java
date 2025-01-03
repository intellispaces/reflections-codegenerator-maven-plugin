package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

record GenericQualifierAppointmentImpl(
    String alias,
    DomainReference actualDomain
) implements GenericQualifierAppointment {
}
