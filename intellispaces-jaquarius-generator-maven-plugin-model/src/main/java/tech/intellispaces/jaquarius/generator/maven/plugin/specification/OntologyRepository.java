package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * The ontology repository.
 */
public interface OntologyRepository {

  Domain getDomainByName(String domainName) throws MojoExecutionException;
}
