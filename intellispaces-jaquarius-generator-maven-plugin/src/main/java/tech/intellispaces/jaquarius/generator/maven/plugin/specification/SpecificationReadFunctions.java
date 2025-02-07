package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.commons.base.data.Dictionaries;
import tech.intellispaces.core.specification.Specification;
import tech.intellispaces.core.specification.SpecificationParseFunctions;
import tech.intellispaces.core.specification.exception.SpecificationException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class SpecificationReadFunctions {

  public static Specification readSpecification(Path specPath, Configuration cfg) throws MojoExecutionException {
    if (specPath.toString().toLowerCase().endsWith(".yaml")) {
      return readYamlSpecification(specPath, cfg);
    }
    throw new MojoExecutionException("Unsupported extension of specification file " + specPath);
  }

  @SuppressWarnings("unchecked")
  static Specification readYamlSpecification(Path specPath, Configuration cfg) throws MojoExecutionException {
    try {
      return SpecificationParseFunctions.parseSpecification(
          specPath,
          Paths.get(cfg.settings().projectPath()),
          is -> Dictionaries.get((Map<String, Object>) YAML.load(is))
      );
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Unable to parse specification file " + specPath, e);
    }
  }

  private SpecificationReadFunctions() {}

  private static final Yaml YAML = new Yaml();
}
