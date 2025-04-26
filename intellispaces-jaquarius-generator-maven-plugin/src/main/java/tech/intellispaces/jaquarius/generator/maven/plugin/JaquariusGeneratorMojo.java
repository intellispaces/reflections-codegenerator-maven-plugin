package tech.intellispaces.jaquarius.generator.maven.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import tech.intellispaces.commons.collection.ArraysFunctions;
import tech.intellispaces.commons.collection.CollectionFunctions;
import tech.intellispaces.commons.exception.NotImplementedExceptions;
import tech.intellispaces.commons.properties.PropertiesSet;
import tech.intellispaces.commons.text.StringFunctions;
import tech.intellispaces.jaquarius.Jaquarius;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.ConfigurationLoaderFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Settings;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.SettingsProvider;
import tech.intellispaces.jaquarius.generator.maven.plugin.generation.GenerationFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationReadFunctions;
import tech.intellispaces.jaquarius.settings.OntologyDescription;
import tech.intellispaces.jaquarius.settings.SettingsFunctions;
import tech.intellispaces.specification.space.FileSpecification;
import tech.intellispaces.specification.space.Specification;
import tech.intellispaces.specification.space.repository.InMemorySpaceRepository;
import tech.intellispaces.specification.space.repository.SpaceRepository;
import tech.intellispaces.specification.space.repository.UnitedSpaceRepository;

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
    Path specPath = null;
    try {
      Settings settings = createSettings();

      var unitedRepository = new UnitedSpaceRepository();
      Configuration cfg = createConfiguration(settings, unitedRepository);
      customizeBasicOntology();

      specPath = Paths.get(cfg.settings().specificationPath());
      FileSpecification spec = SpecificationReadFunctions.readSpecification(specPath);
      unitedRepository.addRepository(new InMemorySpaceRepository(spec.ontology()));

      addOntologyRepositories(unitedRepository, cfg);

      GenerationFunctions.generateArtifacts(spec, cfg);

      project.addCompileSourceRoot(cfg.settings().outputDirectory());
    } catch (MojoExecutionException e) {
      getLog().error("Failed to execute Jaquarius Generator plugin. Source specification: " + specPath, e);
      throw e;
    } catch (Exception e) {
      getLog().error("Failed to execute Jaquarius Generator plugin. Source specification: " + specPath, e);
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

  void customizeBasicOntology() throws MojoExecutionException {
    OntologyDescription ontology = readOntologyDescription();
    Jaquarius.ontologyDescription(ontology);
  }

  OntologyDescription readOntologyDescription() throws MojoExecutionException {
    // Try to direct read
    try {
      PropertiesSet props = SettingsFunctions.loadOntologyDescriptionProps(project.getBasedir().toString());
      return SettingsFunctions.parseOntologyDescription(props);
    } catch (IOException e) {
      // ignore
    }

    // Try to read from classpath
    try {
      PropertiesSet props = SettingsFunctions.loadOntologyDescriptionProps(projectClassLoader());
      return SettingsFunctions.parseOntologyDescription(props);
    } catch (Exception e) {
      throw new MojoExecutionException("Could not to load file ontology.description", e);
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
