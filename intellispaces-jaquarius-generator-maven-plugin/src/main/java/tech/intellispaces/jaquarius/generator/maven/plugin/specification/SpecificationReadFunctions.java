package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.commons.base.data.Dictionaries;
import tech.intellispaces.core.specification.Specification;
import tech.intellispaces.core.specification.SpecificationParseFunctions;
import tech.intellispaces.core.specification.exception.SpecificationException;

import java.nio.file.Path;
import java.util.Map;

public class SpecificationReadFunctions {

  public static Specification readSpecification(Path specPath) throws MojoExecutionException {
    if (specPath.toString().toLowerCase().endsWith(".yaml")) {
      return readYamlSpecification(specPath);
    }
    throw new MojoExecutionException("Unsupported extension of specification file " + specPath);
  }

  @SuppressWarnings("unchecked")
  static Specification readYamlSpecification(Path specPath) throws MojoExecutionException {
    try {
      return SpecificationParseFunctions.parseSpecification(
          specPath,
          is -> Dictionaries.get((Map<String, Object>) YAML.load(is))
      );
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Unable to parse specification file " + specPath, e);
    }
  }

  private SpecificationReadFunctions() {}

  private static final Yaml YAML = new Yaml();
}
