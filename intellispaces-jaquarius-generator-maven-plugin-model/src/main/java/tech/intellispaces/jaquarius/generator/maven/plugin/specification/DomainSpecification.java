package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public interface DomainSpecification {

  String name();

  String did();

  String description();

  List<GenericQualifierSpecification> genericQualifiers();

  List<DomainReference> parents();

  List<DomainChannelSpecification> channels();
}

