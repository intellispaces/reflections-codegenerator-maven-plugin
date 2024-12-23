package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.jaquarius.generator.maven.plugin.propeties.Dictionaries;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class YamlSpecificationFunctions {
  private static final Yaml YAML = new Yaml();

  public static Specification read(String specificationPath) throws MojoExecutionException {
    try {
      Map<String, Object> map = YAML.load(new FileInputStream(specificationPath));
      return SpecificationFunctions.read(Dictionaries.get(map));
    } catch (FileNotFoundException e){
      throw new MojoExecutionException(e);
    }
  }

  private YamlSpecificationFunctions() {}
}
