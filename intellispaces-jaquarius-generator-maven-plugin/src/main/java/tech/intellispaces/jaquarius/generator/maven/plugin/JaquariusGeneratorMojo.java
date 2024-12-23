package tech.intellispaces.jaquarius.generator.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.ConfigurationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.SettingsProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.generation.GenerationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationFunctions;

@Mojo(name = "jaquarius-generator", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class JaquariusGeneratorMojo extends AbstractMojo {

  /**
   * The specification file path.
   */
  @Parameter(property = "inputSpec", required = true)
  private String inputSpec;

  /**
   * The package name.
   */
  @Parameter(property = "packageName")
  private String packageName;

  /**
   * The directory for generated Java source files.
   */
  @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/jaquarius")
  private String outputDirectory;

  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    Configuration cfg = readConfiguration();

    Specification spec = SpecificationFunctions.read(cfg.settings());

    GenerationFunctions.generate(spec, cfg);

    project.addCompileSourceRoot(cfg.settings().outputDirectory());
  }

  Configuration readConfiguration() {
    var settings = SettingsProvider.builder()
        .specificationPath(inputSpec)
        .packageName(packageName)
        .outputDirectory(outputDirectory)
        .get();
    return ConfigurationFunctions.read(settings);
  }
}
