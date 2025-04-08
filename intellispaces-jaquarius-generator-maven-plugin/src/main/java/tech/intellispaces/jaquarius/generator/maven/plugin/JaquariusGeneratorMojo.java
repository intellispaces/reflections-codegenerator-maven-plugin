package tech.intellispaces.jaquarius.generator.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.commons.collection.ArraysFunctions;
import tech.intellispaces.commons.collection.CollectionFunctions;
import tech.intellispaces.commons.data.Dictionary;
import tech.intellispaces.commons.exception.NotImplementedExceptions;
import tech.intellispaces.commons.text.StringFunctions;
import tech.intellispaces.specification.space.Specification;
import tech.intellispaces.specification.space.repository.InMemorySpaceRepository;
import tech.intellispaces.specification.space.repository.SpaceRepository;
import tech.intellispaces.specification.space.repository.UnitedSpaceRepository;
import tech.intellispaces.jaquarius.Jaquarius;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.ConfigurationLoaderFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.SettingsProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.generation.GenerationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationReadFunctions;
import tech.intellispaces.jaquarius.settings.JaquariusSettings;
import tech.intellispaces.jaquarius.settings.JaquariusSettingsFunctions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

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
   * The external ontology repositories.
   */
  @Parameter(property = "repositories", required = false)
  private String[] repositories;

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

      var unitedRepository = new UnitedSpaceRepository();
      Configuration cfg = createConfiguration(settings, unitedRepository);
      customizeJaquariusSettings();

      Path specPath = Paths.get(cfg.settings().specificationPath());
      Specification spec = SpecificationReadFunctions.readSpecification(specPath);
      unitedRepository.addRepository(new InMemorySpaceRepository(spec.ontology()));

      addOntologyRepositories(unitedRepository, cfg);

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

  Configuration createConfiguration(
      Settings settings, SpaceRepository repository
  ) throws MojoExecutionException {
    return ConfigurationLoaderFunctions.loadConfiguration(
        settings,
        repository,
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

  void addOntologyRepositories(
      UnitedSpaceRepository unitedRepository, Configuration cfg
  ) throws MojoExecutionException {
    if (ArraysFunctions.isNullOrEmpty(repositories)) {
      return;
    }
    for (String repositoryUrl : repositories) {
      if (repositoryUrl.startsWith("file://")) {
        addFileOntologyRepository(unitedRepository, repositoryUrl, cfg);
      } else {
        throw NotImplementedExceptions.withCode("WkYWoTxe");
      }
    }
  }

  void addFileOntologyRepository(
      UnitedSpaceRepository unitedRepository, String repositoryUrl, Configuration cfg
  ) throws MojoExecutionException {
      String normRepositoryUrl = repositoryUrl.replace('\\', '/');
      var specPath = Path.of(StringFunctions.removeHeadIfPresent(URI.create(normRepositoryUrl).getPath(), "/"));
      Specification spec = SpecificationReadFunctions.readSpecification(specPath);
      unitedRepository.addRepository(new InMemorySpaceRepository(spec.ontology()));
  }

  void customizeJaquariusSettings() throws MojoExecutionException {
    JaquariusSettings jaquariusSettings = readJaquariusSettings();
    Jaquarius.settings(jaquariusSettings);
  }

  JaquariusSettings readJaquariusSettings() throws MojoExecutionException {
    // Try to direct read
    try {
      Dictionary dictionary = JaquariusSettingsFunctions.readSettingsDictionary(project.getBasedir().toString());
      return JaquariusSettingsFunctions.buildSettings(dictionary);
    } catch (IOException e) {
      // ignore
    }

    // Try to read from classpath
    try {
      Dictionary dictionary = JaquariusSettingsFunctions.readSettingsDictionary(projectClassLoader());
      return JaquariusSettingsFunctions.buildSettings(dictionary);
    } catch (Exception e) {
      throw new MojoExecutionException("Could not to load file basic_domain.properties", e);
    }
  }

  @SuppressWarnings("unchecked")
  ClassLoader projectClassLoader() throws MojoExecutionException {
    try {
      List<URL> urls = CollectionFunctions.mapEach(
          (Set<Artifact>) project.getDependencyArtifacts(), a -> a.getFile().toURI().toURL());
      return new URLClassLoader(urls.toArray(new URL[0]), ConfigurationLoaderFunctions.class.getClassLoader());
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not get project classloader", e);
    }
  }
}
