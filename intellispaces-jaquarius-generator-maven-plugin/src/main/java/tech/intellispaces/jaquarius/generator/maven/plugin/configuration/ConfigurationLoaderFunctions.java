package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionary;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.OntologyRepository;
import tech.intellispaces.jaquarius.space.domain.PrimaryDomainSet;
import tech.intellispaces.jaquarius.space.domain.PrimaryDomains;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface ConfigurationLoaderFunctions {

  static Configuration loadConfiguration(
      MavenProject project,
      Settings pluginSettings,
      OntologyRepository repository,
      Log log
  ) throws MojoExecutionException {
    var builder = SettingsProvider.builder();
    builder.projectPath(pluginSettings.projectPath());
    builder.specificationPath(pluginSettings.specificationPath());
    builder.outputDirectory(pluginSettings.outputDirectory());
    builder.coreDomains(readPrimaryDomains(project));
    Settings settings = builder.get();
    return Configurations.build()
        .settings(settings)
        .repository(repository)
        .log(log)
        .get();
  }

  static PrimaryDomainSet readPrimaryDomains(MavenProject project) throws MojoExecutionException {
    var dictionaries = new ArrayList<Dictionary>();

    // Try to direct read
    try {
      dictionaries.add(PrimaryDomains.readPrimaryDomainDictionary(project.getBasedir().toString()));
    } catch (IOException e) {
       // ignore
    }

    // Try to read from classpath
    try {
      dictionaries.addAll(PrimaryDomains.readPrimaryDomainDictionaries(projectClassLoader(project)));
    } catch (Exception e) {
      throw new MojoExecutionException("Could not to load file domain.properties", e);
    }
    return PrimaryDomains.get(dictionaries);
  }

  @SuppressWarnings("unchecked")
  static ClassLoader projectClassLoader(MavenProject project) throws MojoExecutionException {
    try {
      List<URL> urls = CollectionFunctions.mapEach(
          (Set<Artifact>) project.getDependencyArtifacts(), a -> a.getFile().toURI().toURL());
      return new URLClassLoader(urls.toArray(new URL[0]), ConfigurationLoaderFunctions.class.getClassLoader());
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not get project classloader", e);
    }
  }
}
