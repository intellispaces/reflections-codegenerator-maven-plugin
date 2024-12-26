package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0;

public interface GenerationFunctions {

  static void generateArtifacts(Specification spec, Configuration cfg) throws MojoExecutionException {
    cfg.log().info("Process specification " + spec.path());
    switch (SpecificationVersions.from(spec.version())) {
      case V0p0:
        GenerationFunctionsV0.generateArtifacts((SpecificationV0) spec, cfg);
        break;
      default:
        throw new MojoExecutionException("Unsupported specification version " + spec.version().naming());
    }
  }
}
