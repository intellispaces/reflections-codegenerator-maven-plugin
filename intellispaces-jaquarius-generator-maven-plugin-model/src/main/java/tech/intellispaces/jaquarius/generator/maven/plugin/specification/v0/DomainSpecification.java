package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public interface DomainSpecification {

  String label();

  String did();

  String description();

  List<ParentDomainSpecification> parents();

  List<DomainChannelSpecification> channels();
}

