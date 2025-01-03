package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.general.collection.ArraysFunctions;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionaries;
import tech.intellispaces.general.data.Dictionary;
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
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class YamlSpecificationReadFunctions {
  private static final Yaml YAML = new Yaml();

  public static Specification readSpecification(
      Path specPath, Configuration cfg
  ) throws MojoExecutionException {
    var specs = new LinkedHashMap<String, Specification>();
    readRelatedSpecifications(specPath, cfg, specs);
    return joinSpecification(specPath, specs.values());
  }

  static void readRelatedSpecifications(
      Path specPath,
      Configuration cfg,
      Map<String, Specification> specs
  ) throws MojoExecutionException {
    if (!specs.containsKey(specPath.toString())) {
      Dictionary specDictionary = readSpecificationDictionary(specPath);
      Specification spec = readSpecification(specPath, specDictionary);
      specs.put(specPath.toString(), spec);

      List<Path> importSpecPaths = readImports(specDictionary, cfg);
      CollectionFunctions.forEach(importSpecPaths,
          importSpecPath -> readRelatedSpecifications(importSpecPath, cfg, specs));
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

  static OntologySpecification readOntology(Dictionary specDictionary) throws MojoExecutionException {
    Dictionary ontologyDictionary = specDictionary.dictionaryValue("ontology");
    return OntologySpecifications.build()
        .domains(readDomains(ontologyDictionary))
        .get();
  }

  static List<DomainSpecification> readDomains(Dictionary ontologyDictionary) throws MojoExecutionException {
    if (!ontologyDictionary.hasProperty("domains")) {
      return List.of();
    }
    List<Dictionary> domainDictionaries = ontologyDictionary.dictionaryListValue("domains");
    return CollectionFunctions.mapEach(domainDictionaries, YamlSpecificationReadFunctions::readDomain);
  }

  static DomainSpecification readDomain(Dictionary domainDictionary) throws MojoExecutionException {
    return DomainSpecifications.build()
        .name(domainDictionary.name())
        .did(domainDictionary.stringValue("did"))
        .description(domainDictionary.stringValueNullable("description"))
        .genericQualifiers(readGenericQualifiers(domainDictionary))
        .parents(readParentDomains(domainDictionary))
        .channels(readDomainChannels(domainDictionary))
        .get();
  }

  static List<DomainReference> readParentDomains(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("parents")) {
      return List.of();
    }
    List<Dictionary> parentDictionaries = domainDictionary.dictionaryListValue("parents");
    return CollectionFunctions.mapEach(parentDictionaries,
        dict -> YamlSpecificationReadFunctions.readDomainReference(dict, null));
  }

  static DomainReference readDomainReference(
      Dictionary dictionary, String... prefix
  ) throws MojoExecutionException {
    var builder = DomainReferences.build();
    builder.alias(traverseToString(dictionary, ArraysFunctions.join(prefix, "alias")));
    builder.name(traverseToString(dictionary, ArraysFunctions.join(prefix, "name")));

    List<Dictionary> genericQualifierDictionaries = traverseToDictionariesList(
        dictionary, ArraysFunctions.join(prefix, "qualifiers", "generic")
    );
    if (genericQualifierDictionaries != null) {
      builder.genericQualifiers(readGenericQualifierAppointments(genericQualifierDictionaries));
    }
    return builder.get();
  }

  static ValueReference readValueReference(
      Dictionary dictionary, String... prefix
  ) {
    if (dictionary == null) {
      return null;
    }
    var builder = ValueReferences.build();
    builder.alias(traverseToString(dictionary, ArraysFunctions.join(prefix, "alias")));
    return builder.get();
  }

  static List<GenericQualifierSpecification> readGenericQualifiers(
      Dictionary dictionary
  ) throws MojoExecutionException {
    List<Dictionary> genericQualifierDictionaries = traverseToDictionariesList(dictionary, "qualifiers", "generic");
    if (genericQualifierDictionaries == null) {
      return List.of();
    }
    return CollectionFunctions.mapEach(genericQualifierDictionaries,
        YamlSpecificationReadFunctions::readGenericQualifier);
  }

  static GenericQualifierSpecification readGenericQualifier(
      Dictionary qualifierDictionary
  ) throws MojoExecutionException {
    List<Dictionary> extendedDomainSpecs = traverseToDictionariesList(qualifierDictionary, "extends");
    List<DomainReference> extendedDomains = CollectionFunctions.mapEach(extendedDomainSpecs,
        extendedDomainSpec -> readDomainReference(extendedDomainSpec, "domain"));

    return GenericQualifierSpecifications.build()
        .alias(readDictionaryAlias(qualifierDictionary))
        .extendedDomains(extendedDomains != null ? extendedDomains : List.of())
        .get();
  }

  static List<GenericQualifierAppointment> readGenericQualifierAppointments(
      List<Dictionary> genericQualifierDictionaries
  ) throws MojoExecutionException {
    return CollectionFunctions.mapEach(genericQualifierDictionaries,
        YamlSpecificationReadFunctions::readGenericQualifierAppointment
    );
  }

  static GenericQualifierAppointment readGenericQualifierAppointment(
      Dictionary genericQualifierDictionary
  ) throws MojoExecutionException {
    String alias = readDictionaryAlias(genericQualifierDictionary);
    return GenericQualifierAppointments.build()
        .alias(alias)
        .actualDomain(readDomainReference(genericQualifierDictionary, "domain"))
        .get();
  }

  static List<DomainChannelSpecification> readDomainChannels(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("channels")) {
      return List.of();
    }
    List<Dictionary> parentProperties = domainDictionary.dictionaryListValue("channels");
    return CollectionFunctions.mapEach(parentProperties, YamlSpecificationReadFunctions::readDomainChannel);
  }

  static DomainChannelSpecification readDomainChannel(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    return DomainChannelSpecifications.build()
        .alias(readDictionaryAlias(channelDictionary))
        .name(channelDictionary.stringValueNullable("name"))
        .cid(channelDictionary.stringValue("cid"))
        .targetDomain(readDomainReference(channelDictionary, "target", "domain"))
        .targetValue(readValueReference(channelDictionary, "target", "value"))
        .valueQualifiers(readChannelValueQualifiers(channelDictionary))
        .allowedTraverses(readAllowedTraverses(channelDictionary))
        .get();
  }

  static List<ValueQualifierSpecification> readChannelValueQualifiers(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    List<Dictionary> qualifierDictionaries = traverseToDictionariesList(channelDictionary, "qualifiers", "value");
    if (qualifierDictionaries == null) {
      return List.of();
    }
    return CollectionFunctions.mapEach(qualifierDictionaries,
        YamlSpecificationReadFunctions::readChannelValueQualifier);
  }

  static ValueQualifierSpecification readChannelValueQualifier(
      Dictionary valueQualifierDictionary
  ) throws MojoExecutionException {
    return ValueQualifierSpecifications.build()
        .name(readDictionaryAlias(valueQualifierDictionary))
        .domain(readDomainReference(valueQualifierDictionary, "domain"))
        .get();
  }

  static List<String> readAllowedTraverses(Dictionary channelDictionary) {
    return channelDictionary.stringListValueNullable("allowedTraverses");
  }

  static SpecificationVersion readVersion(Dictionary specDictionary) throws MojoExecutionException {
    String version = specDictionary.stringValue("intellispaces");
    return SpecificationVersions.from(version);
  }

  static List<Path> readImports(Dictionary specDictionary, Configuration cfg) {
    if (!specDictionary.hasProperty("imports")) {
      return List.of();
    }
    List<String> importPathPatterns = specDictionary.stringListValue("imports");
    return readImports(Paths.get(cfg.settings().projectPath()), importPathPatterns);
  }

  static List<Path> readImports(Path projectPath, List<String> importPathPatterns) {
    var files = new ArrayList<Path>();
    CollectionFunctions.forEach(importPathPatterns, importMask -> readImports(projectPath, importMask, files));
    return files;
  }

  static void readImports(Path projectPath, String importPathPattern, List<Path> files) {
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
        .ontology(OntologySpecifications.build()
            .domains(specs.stream()
                .map(Specification::ontology)
                .map(OntologySpecification::domains)
                .flatMap(List::stream)
                .toList())
            .get())
        .get();
  }

  static String traverseToString(Dictionary dictionary, String... pathParts) {
    return traverse(dictionary, Dictionary::stringValue, pathParts);
  }

  static List<Dictionary> traverseToDictionariesList(Dictionary dictionary, String... pathParts) {
    return traverse(dictionary, Dictionary::dictionaryListValue, pathParts);
  }

  static <T> T traverse(Dictionary dictionary, BiFunction<Dictionary, String, T> targetMapper, String... pathParts) {
    String path = Arrays.stream(pathParts).filter(Objects::nonNull).collect(Collectors.joining("."));
    List<String> parts = StringFunctions.splitAndTrim(path, ".");
    String propertyName = "";
    for (int ind = 0; ind < parts.size(); ind++) {
      String part = parts.get(ind);
      propertyName = propertyName + (propertyName.isEmpty() ? "" : ".") + part;
      if (dictionary.hasProperty(propertyName)) {
        if (ind == parts.size() - 1) {
          return targetMapper.apply(dictionary, propertyName);
        }
        dictionary = dictionary.dictionaryValue(propertyName);
        propertyName = "";
      }
    }
    return null;
  }

  private YamlSpecificationReadFunctions() {}
}
