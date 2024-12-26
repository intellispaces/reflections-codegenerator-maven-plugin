package tech.intellispaces.jaquarius.generator.maven.plugin.dictionary;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.text.StringFunctions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class MapBasedDictionary implements Dictionary {
  private final String path;
  private final String name;
  private final LinkedHashMap<String, Object> map;

  MapBasedDictionary(String path, String name, LinkedHashMap<String, Object> map) {
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
  public List<String> properties() {
    return new ArrayList<>(map.keySet());
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
  public boolean hasValue(String propertyName) {
    return map.get(propertyName) != null;
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
    if (value instanceof LinkedHashMap<?,?>) {
      return Dictionaries.get(joinPath(propertyName), propertyName, (LinkedHashMap<String, Object>) value);
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
    if (value instanceof LinkedHashMap) {
      var valueMap = (LinkedHashMap<String, Object>) value;
      return CollectionFunctions.mapEach(valueMap.entrySet(), e -> {
        if (e.getValue() instanceof LinkedHashMap) {
          return Dictionaries.get(
              joinPath(propertyName, e.getKey()),
              e.getKey(),
              (LinkedHashMap<String, Object>) e.getValue()
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
              new LinkedHashMap<>()
          );
        } else if (v instanceof LinkedHashMap) {
          var map = (LinkedHashMap<String, Object>) v;
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
