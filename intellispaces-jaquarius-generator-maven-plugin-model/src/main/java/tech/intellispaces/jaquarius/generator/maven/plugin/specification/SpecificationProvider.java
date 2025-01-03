package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;

public interface SpecificationProvider {

  DomainSpecification getDomainByName(String domainName) throws MojoExecutionException;
}
