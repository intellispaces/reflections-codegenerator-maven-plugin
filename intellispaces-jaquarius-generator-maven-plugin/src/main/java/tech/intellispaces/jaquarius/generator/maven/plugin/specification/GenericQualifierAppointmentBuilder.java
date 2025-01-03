package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public class GenericQualifierAppointmentBuilder {
  private String alias;
  private DomainReference actualDomain;

  public GenericQualifierAppointmentBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public GenericQualifierAppointmentBuilder actualDomain(DomainReference domain) {
    this.actualDomain = domain;
    return this;
  }

  public GenericQualifierAppointment get() {
    return new GenericQualifierAppointmentImpl(
        alias,
        actualDomain
    );
  }
}
