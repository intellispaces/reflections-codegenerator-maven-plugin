package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.general.entity.Enumeration;

public enum DomainPurposes implements Enumeration<DomainPurpose>, DomainPurpose {

  String("java.lang.String"),

  Integer("java.lang.Integer"),

  Domain("tech.intellispaces.general.type.Type");

  private final String classname;

  DomainPurposes(java.lang.String classname) {
    this.classname = classname;
  }

  public java.lang.String className() {
    return classname;
  }
}
