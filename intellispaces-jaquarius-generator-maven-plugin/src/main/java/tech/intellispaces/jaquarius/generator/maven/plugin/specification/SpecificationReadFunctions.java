package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;

import java.nio.file.Path;

public interface SpecificationReadFunctions {

  static Specification readSpecification(Path specPath, Configuration cfg) throws MojoExecutionException {
    if (specPath.toString().toLowerCase().endsWith(".yaml")) {
      return YamlSpecificationReadFunctions.readYamlSpecification(specPath, cfg);
    }
    throw new MojoExecutionException("Unsupported extension of specification file " + specPath);
  }
}
