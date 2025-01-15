package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.general.collection.ArraysFunctions;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionaries;
import tech.intellispaces.general.data.Dictionary;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.exception.UnexpectedExceptions;
import tech.intellispaces.general.text.StringFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class YamlSpecificationReadFunctions {
  private static final Yaml YAML = new Yaml();

  public static Specification readYamlSpecification(
      Path specPath, Configuration cfg
  ) throws MojoExecutionException {
    var specs = new LinkedHashMap<String, Specification>();
    readSpecifications(specPath, cfg, specs);
    return joinSpecification(specPath, specs.values());
  }

  static void readSpecifications(
      Path specPath, Configuration cfg, Map<String, Specification> specs
  ) throws MojoExecutionException {
    if (!specs.containsKey(specPath.toString())) {
      Dictionary specDictionary = readSpecificationDictionary(specPath);

      SpecificationVersion specVersion = readVersion(specDictionary);
      if (!SpecificationVersions.V0p1.is(specVersion)) {
        throw NotImplementedExceptions.withCode("FT4tQA");
      }

      Specification spec = readSpecification(specPath, specDictionary);
      specs.put(specPath.toString(), spec);

      List<Path> importSpecPaths = getImportedSpecifications(specDictionary, cfg);
      CollectionFunctions.forEach(importSpecPaths,
          importSpecPath -> readSpecifications(importSpecPath, cfg, specs));
    }
  }

  static Dictionary readSpecificationDictionary(Path specPath) throws MojoExecutionException {
    try {
      LinkedHashMap<String, Object> map = YAML.load(new FileInputStream(specPath.toFile()));
      return Dictionaries.get(map);
    } catch (FileNotFoundException e){
      throw new MojoExecutionException(e);
    }
  }

  static Specification readSpecification(Path specPath, Dictionary specDictionary) throws MojoExecutionException {
    return Specifications.build(specPath)
        .ontology(readOntology(specDictionary))
        .get();
  }

  static Ontology readOntology(Dictionary specDictionary) throws MojoExecutionException {
    Dictionary ontologyDictionary = specDictionary.dictionaryValue("ontology");
    return Ontologies.build()
        .domains(readDomains(ontologyDictionary))
        .get();
  }

  static List<Domain> readDomains(Dictionary ontologyDictionary) throws MojoExecutionException {
    if (!ontologyDictionary.hasProperty("domains")) {
      return List.of();
    }
    List<Dictionary> domainDictionaries = ontologyDictionary.dictionaryListValue("domains");
    return CollectionFunctions.mapEach(domainDictionaries, YamlSpecificationReadFunctions::readDomain);
  }

  static Domain readDomain(Dictionary domainDictionary) throws MojoExecutionException {
    return Domains.build()
        .name(domainDictionary.name())
        .did(domainDictionary.stringValue("did"))
        .description(domainDictionary.stringValueNullable("description"))
        .superDomains(readSuperDomains(domainDictionary))
        .channels(readDomainChannels(domainDictionary))
        .get();
  }

  static List<DomainReference> readSuperDomains(Dictionary domainDictionary) {
    if (!domainDictionary.hasProperty("superDomains")) {
      return List.of();
    }
    List<Dictionary> dictionaries = domainDictionary.dictionaryListValue("superDomains");
    return CollectionFunctions.mapEach(dictionaries, dict -> readDomainReference(dict));
  }

  static DomainReference readDomainReference(Object value, String... propertyPath) {
    if (value instanceof String name) {
      DomainReferenceBuilder builder = DomainReferences.build();
      builder.name(name);
      return builder.get();
    } else if (value instanceof Dictionary dictionary) {
      return readDomainReference(dictionary, propertyPath);
    } else {
      throw NotImplementedExceptions.withCode("jjm5xyFL");
    }
  }

  static DomainReference readDomainReference(Dictionary dictionary, String... propertyPath) {
    DomainReferenceBuilder builder = DomainReferences.build();
    builder.name(readDomainName(dictionary, propertyPath));
    builder.equivalences(readContextEquivalences(dictionary, propertyPath));
    builder.superDomainBounds(readSuperDomainBounds(dictionary, propertyPath));
    return builder.get();
  }

  static String readDomainName(Dictionary dictionary, String... propertyPath) {
    if (isStringValue(dictionary, propertyPath)) {
      return traverseToString(dictionary, propertyPath);
    } else {
      Dictionary dict = traverseToDictionary(dictionary, propertyPath);
      if (dict != null && !dict.propertyNames().isEmpty()) {
        String firstProperty = dict.propertyNames().get(0);
        if (!dict.hasValue(firstProperty)) {
          return firstProperty;
        }
      }
      return dictionary.name();
    }
  }

  static List<DomainReference> readSuperDomainBounds(Dictionary dictionary, String... propertyPath) {
    List<?> superDomainBoundDictionaries = traverseToList(
        dictionary, ArraysFunctions.join(propertyPath, "bounds", "superDomains")
    );
    if (CollectionFunctions.isNullOrEmpty(superDomainBoundDictionaries)) {
      return null;
    }
    return CollectionFunctions.mapEach(superDomainBoundDictionaries, sdb -> readDomainReference(sdb));
  }

  static List<ContextEquivalence> readContextEquivalences(Dictionary dictionary, String... propertyPath) {
    List<Dictionary> equivalenceDictionaries = traverseToDictionaryList(
        dictionary, ArraysFunctions.join(propertyPath, "equivalences")
    );
    if (equivalenceDictionaries == null) {
      return List.of();
    }
    return CollectionFunctions.mapEach(equivalenceDictionaries, YamlSpecificationReadFunctions::readContextEquivalence);
  }

  static ContextEquivalence readContextEquivalence(Dictionary equivalenceDictionary) {
    return ContextEquivalences.build()
        .projectionAlias(equivalenceDictionary.stringValueNullable("projection"))
        .matchedProjectionAlias(equivalenceDictionary.stringValue("matchedProjection"))
        .get();
  }

  static List<ContextChannel> readDomainChannels(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("channels")) {
      return List.of();
    }
    List<Dictionary> parentProperties = domainDictionary.dictionaryListValue("channels");
    return CollectionFunctions.mapEach(parentProperties, YamlSpecificationReadFunctions::readDomainChannel);
  }

  static ContextChannel readDomainChannel(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    return ContextChannels.build()
        .alias(readDictionaryAlias(channelDictionary))
        .cid(channelDictionary.stringValue("cid"))
        .name(channelDictionary.stringValueNullable("name"))
        .description(channelDictionary.stringValueNullable("description"))
        .projections(readChannelProjections(channelDictionary))
        .targetDomain(readDomainReference(channelDictionary, "target", "domain"))
        .targetAlias(traverseToString(channelDictionary, "target", "alias"))
        .allowedTraverses(readAllowedTraverses(channelDictionary))
        .get();
  }

  static List<ContextChannel> readChannelProjections(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    List<Dictionary> projectionDictionaries = traverseToDictionaryList(channelDictionary, "projections");
    if (projectionDictionaries == null) {
      return List.of();
    }
    return CollectionFunctions.mapEach(projectionDictionaries,
        YamlSpecificationReadFunctions::readChannelProjection);
  }

  static ContextChannel readChannelProjection(
      Dictionary projectionDictionary
  ) throws MojoExecutionException {
    return ContextChannels.build()
        .targetAlias(readDictionaryAlias(projectionDictionary))
        .targetDomain(readDomainReference(projectionDictionary, "domain"))
        .get();
  }

  static List<String> readAllowedTraverses(Dictionary channelDictionary) {
    return channelDictionary.stringListValueNullable("allowedTraverses");
  }

  static SpecificationVersion readVersion(Dictionary specDictionary) throws MojoExecutionException {
    String version = specDictionary.stringValue("intellispaces");
    return SpecificationVersions.from(version);
  }

  static List<Path> getImportedSpecifications(Dictionary specDictionary, Configuration cfg) {
    if (!specDictionary.hasProperty("imports")) {
      return List.of();
    }
    List<String> importPathPatterns = specDictionary.stringListValue("imports");
    return getImportedSpecifications(Paths.get(cfg.settings().projectPath()), importPathPatterns);
  }

  static List<Path> getImportedSpecifications(Path projectPath, List<String> importPathPatterns) {
    var files = new ArrayList<Path>();
    CollectionFunctions.forEach(importPathPatterns, importMask -> getImportedSpecifications(
        projectPath, importMask, files)
    );
    return files;
  }

  static void getImportedSpecifications(Path projectPath, String importPathPattern, List<Path> files) {
    files.add(Paths.get(projectPath.toString(), importPathPattern));
  }

  static String readDictionaryAlias(Dictionary dictionary) throws MojoExecutionException {
    String alias = dictionary.stringValueNullable("alias");
    if (alias != null) {
      return alias;
    }
    List<String> properties = dictionary.propertyNames();
    if (properties.isEmpty()) {
      throw new MojoExecutionException("Invalid description: " + dictionary.path());
    }
    String firstProperty = properties.get(0);
    if (dictionary.hasValue(firstProperty)) {
      throw new MojoExecutionException("Invalid alias: " + dictionary.path());
    }
    return firstProperty;
  }

  static Specification joinSpecification(Path specPath, Collection<Specification> specs) {
    return Specifications.build(specPath)
        .ontology(Ontologies.build()
            .domains(specs.stream()
                .map(Specification::ontology)
                .map(Ontology::domains)
                .flatMap(List::stream)
                .toList())
            .get())
        .get();
  }

  static boolean isStringValue(Dictionary dictionary, String... propertyPath) {
    Object value = traverse(dictionary, propertyPath);
    if (value == null) {
      return false;
    }
    return (value instanceof String);
  }

  static String traverseToString(Dictionary dictionary, String... propertyPath) {
    Object value = traverse(dictionary, propertyPath);
    if (value == null) {
      return null;
    } else if (value instanceof String string) {
      return string;
    }
    throw UnexpectedExceptions.withMessage("Property '{0}' is not string",
        propertyPathString(dictionary, propertyPath));
  }

  static Dictionary traverseToDictionary(Dictionary dictionary, String... propertyPath) {
    Object value = traverse(dictionary, propertyPath);
    if (value == null) {
      return null;
    } else if (value instanceof Dictionary dict) {
      return dict;
    }
    throw UnexpectedExceptions.withMessage("Property '{0}' is not dictionary",
        propertyPathString(dictionary, propertyPath));
  }

  static List<?> traverseToList(Dictionary dictionary, String... propertyPath) {
    Object value = traverse(dictionary, propertyPath);
    if (value == null) {
      return null;
    } else if (value instanceof List<?> list) {
      return list;
    }
    throw UnexpectedExceptions.withMessage("Property '{0}' is not list", propertyPathString(dictionary, propertyPath));
  }

  @SuppressWarnings("unchecked")
  static List<Dictionary> traverseToDictionaryList(Dictionary dictionary, String... propertyPath) {
    List<?> list = traverseToList(dictionary, propertyPath);
    if (list == null) {
      return null;
    }

    Object element = list.get(0);
    if (element instanceof Dictionary) {
      return (List<Dictionary>) list;
    }
    throw UnexpectedExceptions.withMessage("Property '{0}' is not dictionary list",
        propertyPathString(dictionary, propertyPath));
  }

  static Object traverse(Dictionary dictionary, String... propertyPath) {
    if (ArraysFunctions.isNullOrEmpty(propertyPath)) {
      return dictionary;
    }

    List<String> conjointPropertyPath = Arrays.stream(propertyPath)
            .filter(Objects::nonNull)
            .flatMap(p -> StringFunctions.splitAndTrim(p, ".").stream())
            .toList();

    String actualPropertyName = "";
    for (int ind = 0; ind < conjointPropertyPath.size(); ind++) {
      String propertyName = conjointPropertyPath.get(ind);
      actualPropertyName = actualPropertyName + (actualPropertyName.isEmpty() ? "" : ".") + propertyName;
      if (dictionary.hasProperty(actualPropertyName)) {
        Object value = dictionary.valueNullable(actualPropertyName);
        if (ind == conjointPropertyPath.size() - 1) {
          return value;
        }
        if (value instanceof Dictionary) {
          dictionary = (Dictionary) value;
          actualPropertyName = "";
        } else if (value instanceof List<?>) {
          throw NotImplementedExceptions.withCode("vnyVN");
        } else {
          return null;
        }
      }
    }
    return null;
  }

  static String propertyPathString(String... path) {
    if (ArraysFunctions.isNullOrEmpty(path)) {
      return "";
    }
    return String.join("\\", path);
  }

  static String propertyPathString(Dictionary dictionary, String... path) {
    int dictionaryPathLength = dictionary.path() != null ? dictionary.path().size() : 0;
    int pathLength = path != null ? path.length : 0;

    String[] fullPath = new String[dictionaryPathLength + pathLength];
    int ind = 0;
    for (int i = 0; i < dictionaryPathLength; i++) {
      fullPath[ind++] = dictionary.path().get(i);
    }
    for (int i = 0; i < pathLength; i++) {
      fullPath[ind++] = path[i];
    }
    return propertyPathString(fullPath);
  }

  private YamlSpecificationReadFunctions() {}
}
