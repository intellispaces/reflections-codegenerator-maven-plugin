package tech.intellispaces.jaquarius.generator.maven.plugin.propeties;

import tech.intellispaces.jaquarius.generator.maven.plugin.properties.Properties;

import java.util.Map;

public interface PropertiesProvider {

  static Properties get(Map<String, Object> map) {
    return get(null, null, map);
  }

  static Properties get(String path, String name, Map<String, Object> map) {
    return new MapBasedProperties(path, name, map);
  }
}
