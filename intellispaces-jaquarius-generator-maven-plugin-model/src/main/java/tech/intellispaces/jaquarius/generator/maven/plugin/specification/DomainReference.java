package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public interface DomainReference {

  String name();

  String alias();

  List<GenericQualifierAppointment> genericQualifiers();
}
