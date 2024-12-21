package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

public class ConfigurationBuilder {
  private Settings settings;

  public ConfigurationBuilder settings(Settings settings) {
    this.settings = settings;
    return this;
  }

  public Configuration get() {
    return new ConfigurationImpl(settings);
  }
}
