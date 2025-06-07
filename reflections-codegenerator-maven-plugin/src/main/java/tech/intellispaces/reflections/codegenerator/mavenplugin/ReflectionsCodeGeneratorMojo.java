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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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
import tech.intellispaces.reflections.framework.settings.OntologyReferencePoints;
import tech.intellispaces.reflections.framework.settings.OntologyReferences;
import tech.intellispaces.specification.space.FileSpecification;
import tech.intellispaces.specification.space.Specification;
import tech.intellispaces.specification.space.repository.InMemorySpecificationRepository;
import tech.intellispaces.specification.space.repository.SpecificationRepository;
import tech.intellispaces.specification.space.repository.UnitedSpecificationRepository;

@Mojo(
    name = "reflections-codegenerator",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class ReflectionsCodeGeneratorMojo extends AbstractMojo {

  /**
   * The specification file path.
   */
  @Parameter(property = "inputSpec", required = true)
  private String inputSpec;

  /**
   * External ontology repositories.
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
      var unitedRepository = new UnitedSpecificationRepository();
      Configuration cfg = createConfiguration(settings, unitedRepository);
      loadOntologyReferences();

      specPath = Paths.get(cfg.settings().specificationPath());
      FileSpecification spec = SpecificationReadFunctions.readSpecification(specPath);
      unitedRepository.addRepository(new InMemorySpecificationRepository(spec.ontology()));

      addOntologyRepositories(unitedRepository);

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
      Settings settings, SpecificationRepository repository
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

  void addOntologyRepositories(UnitedSpecificationRepository unitedRepository) throws MojoExecutionException {
    if (ArraysFunctions.isNullOrEmpty(repositories)) {
      return;
    }
    for (String repositoryUrl : repositories) {
      if (repositoryUrl.startsWith("file://")) {
        addFileOntologyRepository(unitedRepository, repositoryUrl);
      } else {
        throw NotImplementedExceptions.withCode("WkYWoTxe");
      }
    }
  }

  void addFileOntologyRepository(
      UnitedSpecificationRepository unitedRepository, String repositoryUrl
  ) throws MojoExecutionException {
      String normRepositoryUrl = repositoryUrl.replace('\\', '/');
      var specPath = Path.of(StringFunctions.removeHeadIfPresent(URI.create(normRepositoryUrl).getPath(), "/"));
      Specification spec = SpecificationReadFunctions.readSpecification(specPath);
      unitedRepository.addRepository(new InMemorySpecificationRepository(spec.ontology()));
  }

  void loadOntologyReferences() throws MojoExecutionException {
    List<OntologyReferences> ontologyReferences = new ArrayList<>();

    // Try to direct read
    try {
      ontologyReferences.add(OntologyReferencePoints.load(project.getBasedir().toString()));
    } catch (IOException e) {
      // ignore
    }

    // Try to read from classpath
    try {
      ontologyReferences.addAll(OntologyReferencePoints.load(projectClassLoader()));
    } catch (Exception e) {
      throw new MojoExecutionException("Could not to load ontology references", e);
    }

    ReflectionsNodeFunctions.ontologyReference(OntologyReferencePoints.merge(ontologyReferences));
  }

  ClassLoader projectClassLoader() throws MojoExecutionException {
    try {
      List<URL> urls = CollectionFunctions.mapEach(
          project.getDependencyArtifacts(), a -> a.getFile().toURI().toURL());
      return new URLClassLoader(urls.toArray(new URL[0]), ConfigurationLoaderFunctions.class.getClassLoader());
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not get project classloader", e);
    }
  }
}
