package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

/**
 * The domain specification.
 */
public interface Domain {

  /**
   * The domain identifier.
   */
  String did();

  /**
   * The domain qualified name.
   */
  String name();

  /**
   * The domain description.
   */
  String description();

  /**
   * Super domains.
   */
  List<SuperDomain> superDomains();

  /**
   * Domain channels.
   */
  List<ContextChannel> channels();
}

