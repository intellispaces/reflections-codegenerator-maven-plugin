package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DirectSpecificationProvider implements SpecificationProvider {
  private Map<String, DomainSpecification> nameToDomainSpecification;

  public DirectSpecificationProvider(Specification spec) {
    loadSpecification(spec);
  }

  @Override
  public DomainSpecification getDomainByName(String domainName) {
    return nameToDomainSpecification.get(domainName);
  }

  void loadSpecification(Specification specification) {
    nameToDomainSpecification = specification.ontology().domains().stream()
        .collect(Collectors.toMap(DomainSpecification::name, Function.identity()));
  }
}
