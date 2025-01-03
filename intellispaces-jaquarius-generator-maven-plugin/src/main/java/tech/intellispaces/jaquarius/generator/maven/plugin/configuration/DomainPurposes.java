package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.general.entity.Enumeration;

public enum DomainPurposes implements Enumeration<DomainPurpose>, DomainPurpose {

  /**
   * The domain of strings.
   */
  String("java.lang.String"),

  /**
   * The domain of integer numbers.
   */
  Integer("java.lang.Integer"),

  /**
   * The domain of domains.
   */
  Domain("tech.intellispaces.general.type.Type");

  private final String classname;

  DomainPurposes(java.lang.String classname) {
    this.classname = classname;
  }

  public java.lang.String className() {
    return classname;
  }
}
