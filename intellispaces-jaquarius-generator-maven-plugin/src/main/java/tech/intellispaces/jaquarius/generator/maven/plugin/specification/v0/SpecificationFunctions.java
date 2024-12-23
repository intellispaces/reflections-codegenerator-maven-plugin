package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.properties.Dictionary;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersions;

import java.util.List;

public interface SpecificationFunctions {

  static Specification read(Dictionary spec) throws MojoExecutionException {
    return Specifications.build()
        .version(SpecificationVersions.V0_0)
        .ontology(readOntology(spec))
        .get();
  }

  static OntologySpecification readOntology(Dictionary spec) throws MojoExecutionException {
    Dictionary ontologyDictionary = spec.readProperties("ontology");

    List<Dictionary> domainProperties = ontologyDictionary.readLabeledPropertiesList("domains");
    List<DomainSpecification> domains = CollectionFunctions.mapEach(
        domainProperties, SpecificationFunctions::readDomain
    );

    return OntologySpecificationV0s.build()
        .domains(domains)
        .get();
  }

  static DomainSpecification readDomain(Dictionary domainDictionary) throws MojoExecutionException {
    return DomainSpecifications.build()
        .label(domainDictionary.name())
        .did(domainDictionary.readString("did"))
        .description(domainDictionary.readStringNullable("description"))
        .parents(readDomainParents(domainDictionary))
        .get();
  }

  static List<ParentDomainSpecification> readDomainParents(Dictionary domainDictionary) throws MojoExecutionException {
    if (!domainDictionary.hasProperty("parents")) {
      return List.of();
    }
    List<Dictionary> parentProperties = domainDictionary.readLabeledPropertiesList("parents");
    return CollectionFunctions.mapEach(parentProperties, SpecificationFunctions::readDomainParent);
  }

  static ParentDomainSpecification readDomainParent(Dictionary parentDictionary) {
    return ParentDomainSpecifications.get(parentDictionary.name());
  }
}
