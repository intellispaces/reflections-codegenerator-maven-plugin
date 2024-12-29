package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationProvider;

public class ConfigurationBuilder {
  private Settings settings;
  private SpecificationProvider specificationProvider;
  private Log log;

  public ConfigurationBuilder settings(Settings settings) {
    this.settings = settings;
    return this;
  }

  public ConfigurationBuilder log(Log log) {
    this.log = log;
    return this;
  }

  public ConfigurationBuilder specificationProvider(SpecificationProvider specificationProvider) {
    this.specificationProvider = specificationProvider;
    return this;
  }

  public Configuration get() {
    return new ConfigurationImpl(
        settings,
        specificationProvider,
        log
    );
  }
}
