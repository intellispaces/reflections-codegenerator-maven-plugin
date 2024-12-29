package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionary;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersions;

import java.nio.file.Path;
import java.util.List;

public interface SpecificationV0ReadFunctions {

  static Specification readSpecification(Path path, Dictionary spec) throws MojoExecutionException {
    return Specifications.build(path)
        .version(SpecificationVersions.V0p0)
        .ontology(readOntology(spec))
        .get();
  }

  static OntologySpecification readOntology(Dictionary spec) throws MojoExecutionException {
    Dictionary ontologyDictionary = spec.dictionaryValue("ontology");
    return OntologySpecifications.build()
        .domains(readDomains(ontologyDictionary))
        .get();
  }

  static List<DomainSpecification> readDomains(Dictionary ontologyDictionary) throws MojoExecutionException {
    if (!ontologyDictionary.hasProperty("domains")) {
      return List.of();
    }
    List<Dictionary> domainDictionaries = ontologyDictionary.dictionaryListValue("domains");
    return CollectionFunctions.mapEach(domainDictionaries, SpecificationV0ReadFunctions::readDomain);
  }

  static DomainSpecification readDomain(Dictionary domainDictionary) throws MojoExecutionException {
    return DomainSpecifications.build()
        .label(domainDictionary.name())
        .did(domainDictionary.stringValue("did"))
        .description(domainDictionary.stringValueNullable("description"))
        .genericQualifiers(readGenericQualifiers(domainDictionary))
        .parents(readDomainParents(domainDictionary))
        .channels(readDomainChannels(domainDictionary))
        .get();
  }

  static List<GenericQualifierSpecification> readGenericQualifiers(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("genericQualifiers")) {
      return List.of();
    }
    List<Dictionary> qualifierDictionaries = domainDictionary.dictionaryListValue("genericQualifiers");
    return CollectionFunctions.mapEach(qualifierDictionaries, SpecificationV0ReadFunctions::readGenericQualifier);
  }

  static GenericQualifierSpecification readGenericQualifier(
      Dictionary qualifierDictionary
  ) throws MojoExecutionException {
    return GenericQualifierSpecifications.get(readDictionaryAlias(qualifierDictionary));
  }

  static List<ParentDomainSpecification> readDomainParents(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("parents")) {
      return List.of();
    }
    List<Dictionary> parentDictionaries = domainDictionary.dictionaryListValue("parents");
    return CollectionFunctions.mapEach(parentDictionaries, SpecificationV0ReadFunctions::readDomainParent);
  }

  static ParentDomainSpecification readDomainParent(
      Dictionary parentDictionary
  ) throws MojoExecutionException {
    return ParentDomainSpecifications.build()
        .label(parentDictionary.name())
        .genericQualifierDefinitions(readGenericQualifierDefinitions(
            parentDictionary.dictionaryListValueNullable("genericQualifiers")))
        .get();
  }

  static List<GenericQualifierDefinition> readGenericQualifierDefinitions(
      List<Dictionary> genericQualifierDictionaries
  ) throws MojoExecutionException {
    if (genericQualifierDictionaries == null) {
      return List.of();
    }
    return CollectionFunctions.mapEach(
        genericQualifierDictionaries, SpecificationV0ReadFunctions::readGenericQualifierDefinition
    );
  }

  static GenericQualifierDefinition readGenericQualifierDefinition(
      Dictionary genericQualifierDictionary
  ) throws MojoExecutionException {
    String alias = readDictionaryAlias(genericQualifierDictionary);
    String valueReference = genericQualifierDictionary.stringValueNullable("value.ref");
    return GenericQualifierDefinitions.build()
        .alias(alias)
        .valueReference(valueReference)
        .get();
  }

  static List<DomainChannelSpecification> readDomainChannels(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("channels")) {
      return List.of();
    }
    List<Dictionary> parentProperties = domainDictionary.dictionaryListValue("channels");
    return CollectionFunctions.mapEach(parentProperties, SpecificationV0ReadFunctions::readDomainChannel);
  }

  static DomainChannelSpecification readDomainChannel(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    return DomainChannelSpecifications.build()
        .targetDomainName(readTargetDomainName(channelDictionary))
        .targetDomainRef(readTargetDomainRef(channelDictionary))
        .targetDomainGenericQualifierDefinitions(readGenericQualifierDefinitions(channelDictionary))
        .targetValueRef(readTargetValueRef(channelDictionary))
        .alias(readDictionaryAlias(channelDictionary))
        .cid(channelDictionary.stringValue("cid"))
        .name(channelDictionary.stringValueNullable("name"))
        .allowedTraverses(readAllowedTraverses(channelDictionary))
        .valueQualifiers(readChannelValueQualifiers(channelDictionary))
        .get();
  }

  static List<ValueQualifiedSpecification> readChannelValueQualifiers(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    if (!channelDictionary.hasProperty("valueQualifiers")) {
      return List.of();
    }
    List<Dictionary> qualifierDictionaries = channelDictionary.dictionaryListValue("valueQualifiers");
    return CollectionFunctions.mapEach(qualifierDictionaries, SpecificationV0ReadFunctions::readChannelValueQualifier);
  }

  static ValueQualifiedSpecification readChannelValueQualifier(
      Dictionary qualifierDictionary
  ) throws MojoExecutionException {
    return ValueQualifiedSpecifications.get(
        readDictionaryAlias(qualifierDictionary),
        qualifierDictionary.stringValue("domain.name")
    );
  }

  static String readTargetDomainName(Dictionary channelDictionary) {
    if (channelDictionary.hasProperty("target")) {
      Dictionary targetDictionary = channelDictionary.dictionaryValue("target");
      return targetDictionary.stringValueNullable("domain.name");
    } else if (channelDictionary.hasProperty("target.domain")) {
      Dictionary domainDictionary = channelDictionary.dictionaryValue("target.domain");
      return domainDictionary.stringValueNullable("name");
    }
    return channelDictionary.stringValueNullable("target.domain.name");
  }

  static List<GenericQualifierDefinition> readGenericQualifierDefinitions(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    if (channelDictionary.hasProperty("target")) {
      Dictionary targetDictionary = channelDictionary.dictionaryValue("target");
      if (targetDictionary.hasProperty("domain")) {
        Dictionary domainDictionary = channelDictionary.dictionaryValue("domain");
        if (domainDictionary.hasProperty("genericQualifiers")) {
          List<Dictionary> genericQualifiers = domainDictionary.dictionaryListValue("genericQualifiers");
          return readGenericQualifierDefinitions(genericQualifiers);
        }
        return List.of();
      }
    } else if (channelDictionary.hasProperty("target.domain")) {
      Dictionary domainDictionary = channelDictionary.dictionaryValue("target.domain");
      if (domainDictionary.hasProperty("genericQualifiers")) {
        List<Dictionary> genericQualifiers = domainDictionary.dictionaryListValue("genericQualifiers");
        return readGenericQualifierDefinitions(genericQualifiers);
      }
      return List.of();
    }
    return List.of();
  }

  static String readTargetDomainRef(Dictionary channelDictionary) {
    if (channelDictionary.hasProperty("target")) {
      Dictionary targetDictionary = channelDictionary.dictionaryValue("target");
      return targetDictionary.stringValueNullable("domain.ref");
    }
    return channelDictionary.stringValueNullable("target.domain.ref");
  }

  static String readTargetValueRef(Dictionary channelDictionary) {
    if (channelDictionary.hasProperty("target")) {
      Dictionary targetDictionary = channelDictionary.dictionaryValue("target");
      return targetDictionary.stringValueNullable("value.ref");
    }
    return channelDictionary.stringValueNullable("target.value.ref");
  }

  static List<String> readAllowedTraverses(Dictionary channelDictionary) {
    return channelDictionary.stringListValueNullable("allowedTraverses");
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
      throw new MojoExecutionException("Invalid description: " + dictionary.path());
    }
    return firstProperty;
  }
}
