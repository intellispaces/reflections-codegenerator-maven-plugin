package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

/**
 * The plugin settings.
 */
public interface Settings {

  String projectPath();

  String specificationPath();

  String basePackage();

  String generatedSourcesDirectory();

  String generatedResourcesDirectory();
}
