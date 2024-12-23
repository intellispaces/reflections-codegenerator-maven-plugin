package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationVersions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0;

public interface GenerationFunctions {

  static void generate(Specification spec, Configuration cfg) throws MojoExecutionException {
    switch (SpecificationVersions.from(spec.version())) {
      case V0_0:
        GenerationFunctionsV0.generate((SpecificationV0) spec, cfg);
        break;
      default:
        throw new MojoExecutionException("Unsupported specification version " + spec.version().naming());
    }
  }
}
