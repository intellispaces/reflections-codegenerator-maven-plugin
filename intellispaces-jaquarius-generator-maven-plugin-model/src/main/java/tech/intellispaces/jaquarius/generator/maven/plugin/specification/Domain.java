package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

/**
 * The domain specification.
 */
public interface Domain {

  String did();

  String name();

  String description();

  List<DomainReference> superDomains();

  List<ContextChannel> channels();
}

