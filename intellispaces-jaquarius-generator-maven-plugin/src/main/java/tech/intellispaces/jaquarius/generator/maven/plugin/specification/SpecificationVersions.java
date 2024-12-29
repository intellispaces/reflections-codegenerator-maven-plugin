package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.entity.Enumeration;

public enum SpecificationVersions implements SpecificationVersion, Enumeration<SpecificationVersion> {

  V0p1("0.1");

  private final String naming;

  SpecificationVersions(String naming) {
    this.naming = naming;
  }

  public String naming() {
    return naming;
  }

  public static SpecificationVersions from(String version) throws MojoExecutionException {
    for (SpecificationVersions option : values()) {
      if (option.naming.equals(version)) {
        return option;
      }
    }
    throw new MojoExecutionException("Unsupported version of specification: " + version);
  }

  public static SpecificationVersions from(SpecificationVersion version) {
    return values()[version.ordinal()];
  }
}
