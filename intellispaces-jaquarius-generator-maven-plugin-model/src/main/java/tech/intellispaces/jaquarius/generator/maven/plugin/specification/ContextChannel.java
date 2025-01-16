package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

/**
 * The context channel specification.
 */
public interface ContextChannel {

  /**
   * The channel context alias.
   */
  String alias();

  /**
   * The channel identifier.
   */
  String cid();

  /**
   * The channel qualified name.
   */
  String name();

  /**
   * The channel description.
   */
  String description();

  List<ContextChannel> projections();

  /**
   * The channel target domain.
   */
  DomainReference targetDomain();

  /**
   * The target equivalences.
   */
  List<ContextEquivalence> targetEquivalences();

  /**
   * The channel target alias.
   */
  String targetAlias();

  List<String> allowedTraverse();
}
