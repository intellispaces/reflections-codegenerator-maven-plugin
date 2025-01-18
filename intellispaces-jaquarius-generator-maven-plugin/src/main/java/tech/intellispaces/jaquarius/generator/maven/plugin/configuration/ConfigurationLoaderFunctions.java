package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.OntologyRepository;

public interface ConfigurationLoaderFunctions {

  static Configuration loadConfiguration(
      Settings pluginSettings,
      OntologyRepository repository,
      Log log
  ) {
    var builder = SettingsProvider.builder();
    builder.projectPath(pluginSettings.projectPath());
    builder.specificationPath(pluginSettings.specificationPath());
    builder.outputDirectory(pluginSettings.outputDirectory());
    Settings settings = builder.get();
    return Configurations.build()
        .settings(settings)
        .repository(repository)
        .log(log)
        .get();
  }
}
