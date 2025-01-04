package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.data.Dictionaries;
import tech.intellispaces.general.data.Dictionary;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.resource.ResourceFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationProvider;
import tech.intellispaces.jaquarius.space.domain.CoreDomain;
import tech.intellispaces.jaquarius.space.domain.CoreDomains;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ConfigurationLoaderFunctions {

  static Configuration loadConfiguration(
      MavenProject project,
      Settings pluginSettings,
      SpecificationProvider specificationProvider,
      Log log
  ) throws MojoExecutionException {
    var builder = SettingsProvider.builder();
    builder.projectPath(pluginSettings.projectPath());
    builder.specificationPath(pluginSettings.specificationPath());
    builder.outputDirectory(pluginSettings.outputDirectory());

    List<Settings> projectSettings = readProjectSettings(project);
    if (projectSettings.size() == 1) {
      builder.coreDomains(projectSettings.get(0).coreDomains());
    } else if (projectSettings.size() > 1) {
      throw NotImplementedExceptions.withCode("dertww");
    }
    return Configurations.build()
        .settings(builder.get())
        .specificationProvider(specificationProvider)
        .log(log)
        .get();
  }

  static List<Settings> readProjectSettings(MavenProject project) throws MojoExecutionException {
    // Try to direct read
    try {
      var path = Paths.get(project.getBasedir().toString(),
          "src/main/resources/META-INF/jaquarius/domain.properties"
      );
      String content = Files.readString(path, StandardCharsets.UTF_8);
      return List.of(readDomainProperties(content));
    } catch (IOException e) {
       // ignore
    }

    // Try to read from classpath
    try {
      Enumeration<URL> enumeration = getProjectClassLoader(project).getResources(
          "META-INF/jaquarius/domain.properties");
      List<URL> urls = CollectionFunctions.toList(enumeration);
      return CollectionFunctions.mapEach(urls, url -> readDomainProperties(
          ResourceFunctions.readResourceAsString(url)));
    } catch (Exception e) {
      throw new MojoExecutionException("Could not load domain.properties file", e);
    }
  }

  static Settings readDomainProperties(String content) {
    Dictionary dictionary = Dictionaries.ofProperties(content);
    return SettingsProvider.builder()
        .coreDomains(readCoreDomains(dictionary))
        .get();
  }

  static Map<String, CoreDomain> readCoreDomains(Dictionary dictionary) {
    var map = new HashMap<String, CoreDomain>();
    dictionary.propertyNames().forEach(property -> map.put(
        dictionary.stringValue(property), CoreDomains.valueOf(property))
    );
    return map;
  }

  @SuppressWarnings("unchecked")
  static ClassLoader getProjectClassLoader(MavenProject project) throws MojoExecutionException {
    try {
      List<URL> urls = CollectionFunctions.mapEach(
          (Set<Artifact>) project.getDependencyArtifacts(), a -> a.getFile().toURI().toURL());
      return new URLClassLoader(urls.toArray(new URL[0]), ConfigurationLoaderFunctions.class.getClassLoader());
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not get project classloader", e);
    }
  }
}
