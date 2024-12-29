package tech.intellispaces.jaquarius.generator.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.ConfigurationLoaderFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.SettingsProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.generation.GenerationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.DirectSpecificationV0Provider;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationReadFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.UnitedSpecificationProvider;

import java.util.List;

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
  @Parameter(
      property = "outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/jaquarius",
      required = true)
  private String outputDirectory;

  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      Settings settings = createSettings();

      var unitedSpecificationProvider = new UnitedSpecificationProvider();
      Configuration cfg = createConfiguration(settings, unitedSpecificationProvider);

      List<Specification> specs = SpecificationReadFunctions.readSpecifications(cfg);
      unitedSpecificationProvider.addProvider(new DirectSpecificationV0Provider(specs));

      CollectionFunctions.forEach(specs, spec -> GenerationFunctions.generateArtifacts(spec, cfg));

      project.addCompileSourceRoot(cfg.settings().outputDirectory());
    } catch (MojoExecutionException e) {
      getLog().error("Failed to execute plugin", e);
      throw e;
    } catch (Exception e) {
      getLog().error("Unexpected exception", e);
      throw new MojoExecutionException("Unexpected exception", e);
    }
  }

  Configuration createConfiguration(
      Settings settings,
      SpecificationProvider specificationProvider
  ) throws MojoExecutionException {
    return ConfigurationLoaderFunctions.loadConfiguration(
        project,
        settings,
        specificationProvider,
        getLog()
    );
  }

  Settings createSettings() {
    return SettingsProvider.builder()
        .projectPath(project.getBasedir().toString())
        .specificationPath(inputSpec)
        .outputDirectory(outputDirectory)
        .get();
  }
}
