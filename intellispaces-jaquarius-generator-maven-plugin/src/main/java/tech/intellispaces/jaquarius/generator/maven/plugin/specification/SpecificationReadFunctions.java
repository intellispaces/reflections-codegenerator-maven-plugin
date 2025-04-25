package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.commons.properties.PropertiesSet;
import tech.intellispaces.commons.properties.PropertiesSets;
import tech.intellispaces.specification.space.FileSpecification;
import tech.intellispaces.specification.space.SpecificationParseFunctions;
import tech.intellispaces.specification.space.exception.SpecificationException;

import java.io.InputStream;
import java.nio.file.Path;

public class SpecificationReadFunctions {

  public static FileSpecification readSpecification(Path specPath) throws MojoExecutionException {
    if (specPath.toString().toLowerCase().endsWith(".yaml")) {
      return readYamlSpecification(specPath);
    }
    throw new MojoExecutionException("Unsupported extension of specification file " + specPath);
  }

  static FileSpecification readYamlSpecification(Path specPath) throws MojoExecutionException {
    try {
      return SpecificationParseFunctions.parseSpecification(specPath, SpecificationReadFunctions::parseYaml);
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Unable to parse specification file " + specPath, e);
    }
  }

  private static @NotNull PropertiesSet parseYaml(InputStream is) {
    return PropertiesSets.createFlowing(YAML.load(is), PROPERTY_DELIMITER);
  }

  private SpecificationReadFunctions() {}

  private static final Yaml YAML = new Yaml();
  private static final String PROPERTY_DELIMITER = ".";
}
