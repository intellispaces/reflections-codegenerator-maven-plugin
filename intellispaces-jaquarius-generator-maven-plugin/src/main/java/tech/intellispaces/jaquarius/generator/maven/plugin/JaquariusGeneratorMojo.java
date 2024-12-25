package tech.intellispaces.jaquarius.generator.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.ConfigurationReaderFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.SettingsProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.generation.GenerationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationReadFunctions;

@Mojo(
    name = "jaquarius-generator",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class JaquariusGeneratorMojo extends AbstractMojo {
  /**
   * The specification file path.
   */
  @Parameter(property = "inputSpec", required = true)
  private String inputSpec;

  /**
   * The directory for generated Java source files.
   */
  @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/jaquarius")
  private String outputDirectory;

  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      Configuration cfg = readConfiguration();
      Specification spec = SpecificationReadFunctions.readSpecification(cfg.settings());
      GenerationFunctions.generateArtifacts(spec, cfg);
      project.addCompileSourceRoot(cfg.settings().outputDirectory());
    } catch (MojoExecutionException e) {
      getLog().error("Failed to execute plugin", e);
      throw e;
    } catch (Exception e) {
      getLog().error("Unexpected exception", e);
      throw new MojoExecutionException("Unexpected exception", e);
    }
  }

  Configuration readConfiguration() throws MojoExecutionException {
    var settings = SettingsProvider.builder()
        .specificationPath(inputSpec)
        .outputDirectory(outputDirectory)
        .get();
    return ConfigurationReaderFunctions.readConfiguration(project, settings);
  }
}
