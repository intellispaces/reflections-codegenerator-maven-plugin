package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpecificationRepository;

public interface ConfigurationLoaderFunctions {

  static Configuration loadConfiguration(
      Settings pluginSettings,
      SpecificationRepository repository,
      Log log
  ) {
    var builder = SettingsProvider.builder();
    builder.projectPath(pluginSettings.projectPath());
    builder.specificationPath(pluginSettings.specificationPath());
    builder.basePackage(pluginSettings.basePackage());
    builder.generatedSourcesDirectory(pluginSettings.generatedSourcesDirectory());
    builder.generatedResourcesDirectory(pluginSettings.generatedResourcesDirectory());
    Settings settings = builder.get();
    return Configurations.build()
        .settings(settings)
        .repository(repository)
        .log(log)
        .get();
  }
}
