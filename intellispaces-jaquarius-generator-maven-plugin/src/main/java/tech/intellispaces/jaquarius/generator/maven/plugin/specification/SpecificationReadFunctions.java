package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface SpecificationReadFunctions {

  static Specification readSpecification(Configuration cfg) throws MojoExecutionException {
    Path specPath = Paths.get(cfg.settings().specificationPath());
    if (specPath.toString().endsWith(".yaml")) {
      return YamlSpecificationReadFunctions.readYamlSpecification(specPath, cfg);
    }
    throw new MojoExecutionException("Unsupported extension of specification file " + specPath);
  }
}
