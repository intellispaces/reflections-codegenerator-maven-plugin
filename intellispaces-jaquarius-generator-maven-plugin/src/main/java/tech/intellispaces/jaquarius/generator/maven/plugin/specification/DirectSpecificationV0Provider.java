package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.DomainSpecification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DirectSpecificationV0Provider implements SpecificationProvider {
  private Map<String, DomainSpecification> nameToDomainSpecificationV0;

  public DirectSpecificationV0Provider(List<Specification> specifications) {
    specifications.forEach(this::loadSpecification);
  }

  @Override
  public DomainSpecification domainV0ByName(String domainName) {
    return nameToDomainSpecificationV0.get(domainName);
  }

  void loadSpecification(Specification specification) {
    switch (SpecificationVersions.from(specification.version())) {
      case V0p0 -> loadSpecificationV0((SpecificationV0) specification);
    }
  }

  void loadSpecificationV0(SpecificationV0 specification) {
    nameToDomainSpecificationV0 = specification.ontology().domains().stream()
        .collect(Collectors.toMap(DomainSpecification::label, Function.identity()));
  }
}
