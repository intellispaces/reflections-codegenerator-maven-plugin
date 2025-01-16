package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.List;

public class UnitedOntologyRepository implements OntologyRepository {
  private final List<OntologyRepository> repositories = new ArrayList<>();

  public void addProvider(OntologyRepository repository) {
    this.repositories.add(repository);
  }

  @Override
  public Domain getDomainByName(String domainName) throws MojoExecutionException {
    for (OntologyRepository sp : repositories) {
      Domain domain = sp.getDomainByName(domainName);
      if (domain != null) {
        return domain;
      }
    }
    throw new MojoExecutionException("Could not to find the specification of the domain '" + domainName + "'");
  }
}
