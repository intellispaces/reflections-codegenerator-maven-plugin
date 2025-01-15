package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DirectSpecificationProvider implements SpecificationProvider {
  private Map<String, Domain> nameToDomainSpecification;

  public DirectSpecificationProvider(Specification spec) {
    loadSpecification(spec);
  }

  @Override
  public Domain getDomainByName(String domainName) {
    return nameToDomainSpecification.get(domainName);
  }

  void loadSpecification(Specification specification) {
    nameToDomainSpecification = specification.ontology().domains().stream()
        .collect(Collectors.toMap(Domain::name, Function.identity()));
  }
}
