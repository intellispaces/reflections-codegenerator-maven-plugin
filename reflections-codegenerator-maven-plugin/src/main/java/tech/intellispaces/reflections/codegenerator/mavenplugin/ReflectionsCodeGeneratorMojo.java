package tech.intellispaces.reflections.codegenerator.mavenplugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import tech.intellispaces.commons.text.StringFunctions;
import tech.intellispaces.reflections.codegenerator.mavenplugin.configuration.Configuration;
import tech.intellispaces.reflections.codegenerator.mavenplugin.configuration.ConfigurationLoaderFunctions;
import tech.intellispaces.reflections.codegenerator.mavenplugin.configuration.Settings;
import tech.intellispaces.reflections.codegenerator.mavenplugin.configuration.SettingsProvider;
import tech.intellispaces.reflections.codegenerator.mavenplugin.generation.GenerationFunctions;
import tech.intellispaces.reflections.codegenerator.mavenplugin.specification.SpecificationReadFunctions;
import tech.intellispaces.reflections.framework.node.ReflectionsNodeFunctions;
import tech.intellispaces.reflections.framework.settings.OntologyReference;
import tech.intellispaces.reflections.framework.settings.SettingsFunctions;
import tech.intellispaces.specification.space.FileSpecification;
import tech.intellispaces.specification.space.Specification;
import tech.intellispaces.specification.space.repository.InMemorySpaceRepository;
import tech.intellispaces.specification.space.repository.SpaceRepository;
import tech.intellispaces.specification.space.repository.UnitedSpaceRepository;

@Mojo(
    name = "reflections-codegenerator",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class ReflectionsCodeGeneratorMojo extends AbstractMojo {

  /**
   * The specification file path.
   */
  @Parameter(property = "inputSpec", required = true)
  private String inputSpec;

  /**
   * The external ontology repositories.
   */
  @Parameter(property = "repositories")
  private String[] repositories;

  /**
   * The directory for generated Java source files.
   */
  @Parameter(
      property = "outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/reflections",
      required = true)
  private String outputDirectory;

  /**
   * The base package for generated Java classes.
   */
  @Parameter(property = "basePackage")
  private String basePackage;

  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    Path specPath = null;
    try {
      Settings settings = createSettings();

      var unitedRepository = new UnitedSpaceRepository();
      Configuration cfg = createConfiguration(settings, unitedRepository);
      loadOntologyReferences();

      specPath = Paths.get(cfg.settings().specificationPath());
      FileSpecification spec = SpecificationReadFunctions.readSpecification(specPath);
      unitedRepository.addRepository(new InMemorySpaceRepository(spec.ontology()));

      addOntologyRepositories(unitedRepository, cfg);

      GenerationFunctions.generateArtifacts(spec, cfg);

      project.addCompileSourceRoot(cfg.settings().outputDirectory());
    } catch (MojoExecutionException e) {
      getLog().error("Failed to execute reflectionsJ Generator plugin. Source specification: " + specPath, e);
      throw e;
    } catch (Exception e) {
      getLog().error("Failed to execute reflectionsJ Generator plugin. Source specification: " + specPath, e);
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
        .basePackage(basePackage != null ? basePackage : "")
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

  void loadOntologyReferences() throws MojoExecutionException {
    List<OntologyReference> ontologyReferences = new ArrayList<>();

    // Try to direct read
    try {
      ontologyReferences.add(SettingsFunctions.loadOntologyReference(project.getBasedir().toString()));
    } catch (IOException e) {
      // ignore
    }

    // Try to read from classpath
    try {
      ontologyReferences.addAll(SettingsFunctions.loadOntologyReferences(projectClassLoader()));
    } catch (Exception e) {
      throw new MojoExecutionException("Could not to load ontology references", e);
    }

    OntologyReference ontologyReference = SettingsFunctions.mergeOntologyReferences(ontologyReferences);
    ReflectionsNodeFunctions.ontologyReference(ontologyReference);
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
