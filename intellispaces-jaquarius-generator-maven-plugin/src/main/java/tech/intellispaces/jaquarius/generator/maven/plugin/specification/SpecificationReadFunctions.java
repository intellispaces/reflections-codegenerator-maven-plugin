package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionaries;
import tech.intellispaces.general.data.Dictionary;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0ReadFunctions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpecificationReadFunctions {
  private static final Yaml YAML = new Yaml();

  public static List<Specification> readSpecifications(
      Configuration cfg
  ) throws MojoExecutionException {
    var specs = new LinkedHashMap<String, Specification>();
    readSpecifications(Paths.get(cfg.settings().specificationPath()), cfg, specs);
    return new ArrayList<>(specs.values());
  }

  static void readSpecifications(
      Path specPath,
      Configuration cfg,
      Map<String, Specification> specs
  ) throws MojoExecutionException {
    if (!specs.containsKey(specPath.toString())) {
      Dictionary dictionary = readSpecificationDictionary(specPath);
      Specification spec = readSpecification(specPath, dictionary);
      specs.put(specPath.toString(), spec);

      List<Path> importSpecPaths = readImports(dictionary, cfg);
      CollectionFunctions.forEach(importSpecPaths, importSpecPath -> readSpecifications(importSpecPath, cfg, specs));
    }
  }

  static Dictionary readSpecificationDictionary(Path specPath) throws MojoExecutionException {
    if (specPath.toString().endsWith(".yaml")) {
      return readYamlSpecificationDictionary(specPath);
    }
    throw new MojoExecutionException("Unsupported specification type of specification file " + specPath);
  }

  static Dictionary readYamlSpecificationDictionary(Path specPath) throws MojoExecutionException {
    try {
      LinkedHashMap<String, Object> map = YAML.load(new FileInputStream(specPath.toFile()));
      return Dictionaries.get(map);
    } catch (FileNotFoundException e){
      throw new MojoExecutionException(e);
    }
  }

  static Specification readSpecification(Path specPath, Dictionary spec) throws MojoExecutionException {
    SpecificationVersion version = readVersion(spec);
    return switch (SpecificationVersions.from(version)) {
      case V0p1 -> SpecificationV0ReadFunctions.readSpecification(specPath, spec);
    };
  }

  static SpecificationVersion readVersion(Dictionary spec) throws MojoExecutionException {
    String version = spec.stringValue("intellispaces");
    return SpecificationVersions.from(version);
  }

  static List<Path> readImports(Dictionary dictionary, Configuration cfg) throws MojoExecutionException {
    if (!dictionary.hasProperty("imports")) {
      return List.of();
    }
    List<String> importPathPatterns = dictionary.stringListValue("imports");
    return findImports(Paths.get(cfg.settings().projectPath()), importPathPatterns);
  }

  static List<Path> findImports(
      Path projectPath, List<String> importPathPatterns
  ) {
    var files = new ArrayList<Path>();
    CollectionFunctions.forEach(importPathPatterns, importMask -> findImports(projectPath, importMask, files));
    return files;
  }

  static void findImports(
      Path projectPath, String importPathPattern, List<Path> files
  ) {
    files.add(Paths.get(projectPath.toString(), importPathPattern));
  }

  private SpecificationReadFunctions() {}
}
