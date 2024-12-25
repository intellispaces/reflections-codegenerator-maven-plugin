package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.dictionary.Dictionary;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersions;

import java.util.List;

public interface SpecificationV0p0ReadFunctions {

  static Specification readSpecification(Dictionary spec) throws MojoExecutionException {
    return Specifications.build()
        .version(SpecificationVersions.V0p0)
        .ontology(readOntology(spec))
        .get();
  }

  static OntologySpecification readOntology(Dictionary spec) throws MojoExecutionException {
    Dictionary ontologyDictionary = spec.readDictionary("ontology");
    return OntologySpecifications.build()
        .domains(readDomains(ontologyDictionary))
        .get();
  }

  static List<DomainSpecification> readDomains(Dictionary ontologyDictionary) throws MojoExecutionException {
    if (!ontologyDictionary.hasProperty("domains")) {
      return List.of();
    }
    List<Dictionary> domainDictionaries = ontologyDictionary.readDictionaryList("domains");
    return CollectionFunctions.mapEach(domainDictionaries, SpecificationV0p0ReadFunctions::readDomain);
  }

  static DomainSpecification readDomain(Dictionary domainDictionary) throws MojoExecutionException {
    return DomainSpecifications.build()
        .label(domainDictionary.name())
        .did(domainDictionary.readString("did"))
        .description(domainDictionary.readStringNullable("description"))
        .parents(readDomainParents(domainDictionary))
        .channels(readDomainChannels(domainDictionary))
        .get();
  }

  static List<ParentDomainSpecification> readDomainParents(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("parents")) {
      return List.of();
    }
    List<Dictionary> parentDictionaries = domainDictionary.readDictionaryList("parents");
    return CollectionFunctions.mapEach(parentDictionaries, SpecificationV0p0ReadFunctions::readDomainParent);
  }

  static ParentDomainSpecification readDomainParent(Dictionary parentDictionary) {
    return ParentDomainSpecifications.get(parentDictionary.name());
  }

  static List<DomainChannelSpecification> readDomainChannels(
      Dictionary domainDictionary
  ) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("channels")) {
      return List.of();
    }
    List<Dictionary> parentProperties = domainDictionary.readDictionaryList("channels");
    return CollectionFunctions.mapEach(parentProperties, SpecificationV0p0ReadFunctions::readDomainChannel);
  }

  static DomainChannelSpecification readDomainChannel(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    return DomainChannelSpecifications.build()
        .targetDomainName(channelDictionary.readString("target.domain.name"))
        .alias(channelDictionary.readString("alias"))
        .cid(channelDictionary.readString("cid"))
        .name(channelDictionary.readStringNullable("name"))
        .allowedTraverses(readAllowedTraverses(channelDictionary))
        .qualifiers(readChannelQualifiers(channelDictionary))
        .get();
  }

  static List<ChannelQualifiedSpecification> readChannelQualifiers(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    if (!channelDictionary.hasProperty("qualifiers")) {
      return List.of();
    }
    List<Dictionary> qualifierDictionaries = channelDictionary.readDictionaryList("qualifiers");
    return CollectionFunctions.mapEach(qualifierDictionaries, SpecificationV0p0ReadFunctions::readChannelQualifier);
  }

  static ChannelQualifiedSpecification readChannelQualifier(
      Dictionary qualifierProperty
  ) throws MojoExecutionException {
    return ChannelQualifiedSpecifications.get(
        qualifierProperty.readString("name"),
        qualifierProperty.readString("domain.name")
    );
  }

  static List<String> readAllowedTraverses(
      Dictionary channelDictionary
  ) throws MojoExecutionException {
    return channelDictionary.readStringListNullable("allowedTraverses");
  }
}
