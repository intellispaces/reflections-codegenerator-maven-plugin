package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public interface GenericQualifierSpecification {

  /**
   * The qualifier alias.
   */
  String alias();

  /**
   * Extended domains.
   */
  List<DomainReference> extendedDomains();
}
