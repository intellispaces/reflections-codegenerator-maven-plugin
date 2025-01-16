package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

/**
 * The super domain specification.
 */
public interface SuperDomain {

  DomainReference domain();

  List<ContextEquivalence> equivalences();
}
