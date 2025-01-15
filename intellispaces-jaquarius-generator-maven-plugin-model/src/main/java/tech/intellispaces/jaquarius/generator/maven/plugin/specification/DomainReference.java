package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

/**
 * The domain reference specification.
 */
public interface DomainReference {

  /**
   * The domain name.
   */
  String name();

  /**
   * The domain equivalences.
   */
  List<ContextEquivalence> equivalences();

  List<DomainReference> superDomainBounds();
}
