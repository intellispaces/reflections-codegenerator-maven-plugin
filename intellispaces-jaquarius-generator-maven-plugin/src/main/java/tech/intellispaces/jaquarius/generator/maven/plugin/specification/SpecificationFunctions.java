package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.properties.Dictionary;

public interface SpecificationFunctions {

  static Specification read(Settings settings) throws MojoExecutionException {
    String specPath = settings.specificationPath();
    if (specPath.endsWith(".yaml")) {
      return YamlSpecificationFunctions.read(specPath);
    }
    throw new MojoExecutionException("Unsupported specification type of specification file " + specPath);
  }

  static Specification read(Dictionary spec) throws MojoExecutionException {
    SpecificationVersion version = readVersion(spec);
    return switch (SpecificationVersions.from(version)) {
      case V0_0 -> tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationFunctions.read(spec);
    };
  }

  static SpecificationVersion readVersion(Dictionary spec) throws MojoExecutionException {
    String version = spec.readString("intellispaces");
    return SpecificationVersions.from(version);
  }
}
