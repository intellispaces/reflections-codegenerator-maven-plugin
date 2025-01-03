package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public interface DomainChannelSpecification {

  String alias();

  String cid();

  String name();

  DomainReference targetDomain();

  ValueReference targetValue();

  List<ValueQualifierSpecification> valueQualifiers();

  List<String> allowedTraverse();
}
