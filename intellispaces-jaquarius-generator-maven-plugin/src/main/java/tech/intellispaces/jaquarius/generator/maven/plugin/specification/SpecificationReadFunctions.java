package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.dictionary.Dictionary;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0p0ReadFunctions;

public interface SpecificationReadFunctions {

  static Specification readSpecification(Settings settings) throws MojoExecutionException {
    String specPath = settings.specificationPath();
    if (specPath.endsWith(".yaml")) {
      return YamlSpecificationReadFunctions.readSpecification(specPath);
    }
    throw new MojoExecutionException("Unsupported specification type of specification file " + specPath);
  }

  static Specification readSpecification(Dictionary spec) throws MojoExecutionException {
    SpecificationVersion version = readVersion(spec);
    return switch (SpecificationVersions.from(version)) {
      case V0p0 -> SpecificationV0p0ReadFunctions.readSpecification(spec);
    };
  }

  static SpecificationVersion readVersion(Dictionary spec) throws MojoExecutionException {
    String version = spec.readString("intellispaces");
    return SpecificationVersions.from(version);
  }
}
