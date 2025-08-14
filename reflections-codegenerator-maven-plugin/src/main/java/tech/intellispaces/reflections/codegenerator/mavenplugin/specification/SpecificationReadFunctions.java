package tech.intellispaces.reflections.codegenerator.mavenplugin.specification;

import java.io.InputStream;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;

import tech.intellispaces.commons.properties.PropertiesSets;
import tech.intellispaces.commons.properties.TraversablePropertiesSet;
import tech.intellispaces.specification.space.FileSpecification;
import tech.intellispaces.specification.space.SpecificationParseFunctions;
import tech.intellispaces.specification.space.exception.SpecificationException;

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

  private static TraversablePropertiesSet parseYaml(InputStream is) {
    return PropertiesSets.createFlowingTraversable(YAML.load(is), PROPERTY_DELIMITER);
  }

  private SpecificationReadFunctions() {}

  private static final Yaml YAML = new Yaml();
  private static final String PROPERTY_DELIMITER = ".";
}
