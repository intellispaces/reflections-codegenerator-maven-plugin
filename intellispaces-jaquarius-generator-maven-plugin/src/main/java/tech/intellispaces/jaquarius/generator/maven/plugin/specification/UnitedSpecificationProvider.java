package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.List;

public class UnitedSpecificationProvider implements SpecificationProvider {
  private final List<SpecificationProvider> specificationProviders = new ArrayList<>();

  public void addProvider(SpecificationProvider specificationProvider) {
    this.specificationProviders.add(specificationProvider);
  }

  @Override
  public Domain getDomainByName(String domainName) throws MojoExecutionException {
    for (SpecificationProvider sp : specificationProviders) {
      Domain domain = sp.getDomainByName(domainName);
      if (domain != null) {
        return domain;
      }
    }
    throw new MojoExecutionException("Could not to find the specification of the domain " + domainName);
  }
}
