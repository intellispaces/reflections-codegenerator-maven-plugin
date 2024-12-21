package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.properties.Properties;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0Functions;

public interface SpecificationFunctions {

  static Specification read(Settings settings) throws MojoExecutionException {
    String specPath = settings.specificationPath();
    if (specPath.endsWith(".yaml")) {
      return YamlSpecificationFunctions.read(specPath);
    }
    throw new MojoExecutionException("Unsupported specification type of specification file " + specPath);
  }

  static Specification read(Properties spec) throws MojoExecutionException {
    SpecificationVersion version = readVersion(spec);
    return switch (SpecificationVersions.from(version)) {
      case V0_0 -> SpecificationV0Functions.read(spec);
    };
  }

  static SpecificationVersion readVersion(Properties spec) throws MojoExecutionException {
    String version = spec.readString("intellispaces");
    return SpecificationVersions.from(version);
  }
}
