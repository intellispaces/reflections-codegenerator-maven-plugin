package tech.intellispaces.jaquarius.generator.maven.plugin.dictionary;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.text.StringFunctions;

import java.util.List;
import java.util.Map;

class MapBasedDictionary implements Dictionary {
  private final String path;
  private final String name;
  private final Map<String, Object> map;

  MapBasedDictionary(String path, String name, Map<String, Object> map) {
    this.path = path;
    this.name = name;
    this.map = map;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Dictionary traverse(String path) throws MojoExecutionException {
    if (path == null || path.isEmpty()) {
      return this;
    }
    Dictionary dictionary = this;
    for (String transition : StringFunctions.splitAndTrim(path, ".")) {
      dictionary = dictionary.readDictionary(transition);
    }
    return dictionary;
  }

  @Override
  public boolean hasProperty(String propertyName) {
    return map.containsKey(propertyName);
  }

  @Override
  public String readString(String propertyName) throws MojoExecutionException {
    Object value = map.get(propertyName);
    if (value == null) {
      throw new MojoExecutionException("The property '" + joinPath(propertyName) + "' is not found");
    }
    if (value instanceof String) {
      return (String) value;
    }
    throw new MojoExecutionException("Value of the property " + joinPath(propertyName) + " is not string");
  }

  @Override
  public String readStringNullable(String propertyName) throws MojoExecutionException {
    Object value = map.get(propertyName);
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return (String) value;
    }
    throw new MojoExecutionException("Value of the property " + joinPath(propertyName) + " is not string");
  }

  @Override
  @SuppressWarnings("unchecked")
  public Dictionary readDictionary(String propertyName) throws MojoExecutionException {
    Object value = map.get(propertyName);
    if (value == null) {
      throw new MojoExecutionException("The property '" + joinPath(propertyName) + "' is not found");
    }
    if (value instanceof Map) {
      return Dictionaries.get(joinPath(propertyName), propertyName, (Map<String, Object>) value);
    }
    throw new MojoExecutionException("Value of the property " + joinPath(propertyName) + " is not dictionary");
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Dictionary> readDictionaryListNullable(String propertyName) throws MojoExecutionException {
    Object value = map.get(propertyName);
    if (value == null) {
      return null;
    }
    if (value instanceof Map) {
      var valueMap = (Map<String, Object>) value;
      return CollectionFunctions.mapEach(valueMap.entrySet(), e -> {
        if (e.getValue() instanceof Map) {
          return Dictionaries.get(
              joinPath(propertyName, e.getKey()),
              e.getKey(),
              (Map<String, Object>) e.getValue()
          );
        } else {
          throw new MojoExecutionException("Value of the property " + joinPath(propertyName, e.getKey()) +
              " is not map");
        }
      });
    }
    if (value instanceof List<?> valueList) {
      return CollectionFunctions.mapEach(valueList, (v, index) -> {
        if (v instanceof String) {
          return Dictionaries.get(
              joinPath(propertyName, (String) v),
              (String) v,
              Map.of()
          );
        } else if (v instanceof Map) {
          var map = (Map<String, Object>) v;
          return Dictionaries.get(
              joinPath(propertyName, "[" + index + "]"),
              "[" + index + "]",
              map
          );
        } else {
          throw NotImplementedExceptions.withCode("YTM0NZ");
        }
      });
    }
    throw new MojoExecutionException("Value of the property " + joinPath(propertyName) + " is not list");
  }

  public List<Dictionary> readDictionaryList(String propertyName) throws MojoExecutionException {
    List<Dictionary> dictionaries = readDictionaryListNullable(propertyName);
    if (dictionaries == null) {
      throw new MojoExecutionException("The property '" + joinPath(propertyName) + "' is not found");
    }
    return dictionaries;
  }

  @Override
  public List<String> readStringList(String propertyName) throws MojoExecutionException {
    List<String> list = readStringListNullable(propertyName);
    if (list == null) {
      throw new MojoExecutionException("The property '" + joinPath(propertyName) + "' is not found");
    }
    return list;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> readStringListNullable(String propertyName) throws MojoExecutionException {
    Object value = map.get(propertyName);
    if (value == null) {
      return null;
    }
    if (value instanceof List<?>) {
      return (List<String>) value;
    }
    throw new MojoExecutionException("Value of the property " + joinPath(propertyName) + " is not list");
  }

  String joinPath(String secondPath) {
    return StringFunctions.join(path, secondPath, ".");
  }

  String joinPath(String secondPath, String thirdPath) {
    return StringFunctions.join(path, secondPath, thirdPath, ".");
  }
}
