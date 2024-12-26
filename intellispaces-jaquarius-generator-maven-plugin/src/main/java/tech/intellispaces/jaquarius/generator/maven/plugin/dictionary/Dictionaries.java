package tech.intellispaces.jaquarius.generator.maven.plugin.dictionary;

import java.util.LinkedHashMap;

public interface Dictionaries {

  static Dictionary get(LinkedHashMap<String, Object> map) {
    return get(null, null, map);
  }

  static Dictionary get(String path, String name, LinkedHashMap<String, Object> map) {
    return new MapBasedDictionary(path, name, map);
  }
}
