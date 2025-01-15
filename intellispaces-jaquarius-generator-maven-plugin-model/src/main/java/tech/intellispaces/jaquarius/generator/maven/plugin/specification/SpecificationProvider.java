package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import org.apache.maven.plugin.MojoExecutionException;

public interface SpecificationProvider {

  Domain getDomainByName(String domainName) throws MojoExecutionException;
}
