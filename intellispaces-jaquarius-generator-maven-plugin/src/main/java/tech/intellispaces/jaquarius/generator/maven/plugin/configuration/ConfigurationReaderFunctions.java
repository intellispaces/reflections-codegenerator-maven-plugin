package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.Yaml;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.resource.ResourceFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.dictionary.Dictionaries;
import tech.intellispaces.jaquarius.generator.maven.plugin.dictionary.Dictionary;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ConfigurationReaderFunctions {

  static Configuration readConfiguration(MavenProject project, Settings pluginSettings) throws MojoExecutionException {
    var builder = SettingsProvider.builder();
    builder.specificationPath(pluginSettings.specificationPath());
    builder.outputDirectory(pluginSettings.outputDirectory());

    List<Settings> projectSettings = readProjectSettings(project);
    if (projectSettings.size() == 1) {
      builder.classMapping(projectSettings.get(0).classMapping());
    } else if (projectSettings.size() > 1) {
      throw NotImplementedExceptions.withCode("dertww");
    }
    return Configurations.build()
        .settings(builder.get())
        .get();
  }

  static List<Settings> readProjectSettings(MavenProject project) throws MojoExecutionException {
    try {
      Enumeration<URL> enumeration = getProjectClassLoader(project).getResources(
          "META-INF/jaquarius/jaquarius-generator.yaml");
      List<URL> urls = CollectionFunctions.toList(enumeration);
      var yaml = new Yaml();
      return CollectionFunctions.mapEach(urls, url -> readPredefinedSetting(
          yaml.load(ResourceFunctions.readResourceAsString(url))));
    } catch (Exception e) {
      throw new MojoExecutionException("Could not load jaquarius-generator.yaml files", e);
    }
  }

  static Settings readPredefinedSetting(Map<String, Object> yaml) throws MojoExecutionException {
    Dictionary dictionary = Dictionaries.get(yaml);
    List<Dictionary> classMapping = dictionary.readDictionaryListNullable("classmapping");

    var builder = SettingsProvider.builder();
    if (classMapping != null) {
      builder.classMapping(parseClassMapping(classMapping));
    }
    return builder.get();
  }

  static Map<String, String> parseClassMapping(List<Dictionary> classMapping) throws MojoExecutionException {
    var map = new HashMap<String, String>();
    for (Dictionary mapping : classMapping) {
      map.put(mapping.readString("domain"), mapping.readString("class"));
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  static ClassLoader getProjectClassLoader(MavenProject project) throws MojoExecutionException {
    try {
      List<URL> urls = CollectionFunctions.mapEach(
          (Set<Artifact>) project.getDependencyArtifacts(), a -> a.getFile().toURI().toURL());
      return new URLClassLoader(urls.toArray(new URL[0]), ConfigurationReaderFunctions.class.getClassLoader());
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not get project classloader", e);
    }
  }
}
