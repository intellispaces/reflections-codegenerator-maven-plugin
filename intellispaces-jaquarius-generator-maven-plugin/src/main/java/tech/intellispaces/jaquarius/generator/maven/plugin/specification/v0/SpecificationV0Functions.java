package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.properties.Properties;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersions;

import java.util.List;

public interface SpecificationV0Functions {

  static Specification read(Properties spec) throws MojoExecutionException {
    return SpecificationV0s.build()
        .version(SpecificationVersions.V0_0)
        .ontology(readOntology(spec))
        .get();
  }

  static OntologySpecificationV0 readOntology(Properties spec) throws MojoExecutionException {
    Properties ontologyProperties = spec.readProperties("ontology");

    List<Properties> domainProperties = ontologyProperties.readLabeledPropertiesList("domains");
    List<DomainSpecificationV0> domains = CollectionFunctions.mapEach(
        domainProperties, SpecificationV0Functions::readDomain
    );

    return OntologySpecificationV0s.build()
        .domains(domains)
        .get();
  }

  static DomainSpecificationV0 readDomain(Properties domainProperties) throws MojoExecutionException {
    return DomainSpecificationV0s.build()
        .label(domainProperties.name())
        .did(domainProperties.readString("did"))
        .description(domainProperties.readStringNullable("description"))
        .get();
  }
}
