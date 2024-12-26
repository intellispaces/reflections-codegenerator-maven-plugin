package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;

public class ConfigurationBuilder {
  private Log log;
  private Settings settings;

  public ConfigurationBuilder log(Log log) {
    this.log = log;
    return this;
  }

  public ConfigurationBuilder settings(Settings settings) {
    this.settings = settings;
    return this;
  }

  public Configuration get() {
    return new ConfigurationImpl(
        log,
        settings
    );
  }
}
