package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

public interface ConfigurationFunctions {

  static Configuration read(Settings settings) {

    return Configurations.build()
        .settings(settings)
        .get();
  }
}
