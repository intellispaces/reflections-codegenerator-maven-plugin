package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public interface ParentDomainSpecification {

  String label();

  List<GenericQualifierDefinition> genericQualifierDefinitions();
}
