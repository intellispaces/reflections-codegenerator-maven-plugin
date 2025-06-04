package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpaceRepository;

public interface ConfigurationLoaderFunctions {

  static Configuration loadConfiguration(
      Settings pluginSettings,
      SpaceRepository repository,
      Log log
  ) {
    var builder = SettingsProvider.builder();
    builder.projectPath(pluginSettings.projectPath());
    builder.specificationPath(pluginSettings.specificationPath());
    builder.outputDirectory(pluginSettings.outputDirectory());
    builder.basePackage(pluginSettings.basePackage());
    Settings settings = builder.get();
    return Configurations.build()
        .settings(settings)
        .repository(repository)
        .log(log)
        .get();
  }
}
