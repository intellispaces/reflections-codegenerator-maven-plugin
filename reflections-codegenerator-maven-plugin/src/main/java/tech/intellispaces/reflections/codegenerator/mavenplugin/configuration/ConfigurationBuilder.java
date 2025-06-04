package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpecificationRepository;

public class ConfigurationBuilder {
  private Settings settings;
  private SpecificationRepository repository;
  private Log log;

  public ConfigurationBuilder settings(Settings settings) {
    this.settings = settings;
    return this;
  }

  public ConfigurationBuilder log(Log log) {
    this.log = log;
    return this;
  }

  public ConfigurationBuilder repository(SpecificationRepository repository) {
    this.repository = repository;
    return this;
  }

  public Configuration get() {
    return new ConfigurationImpl(
        settings,
        repository,
        log
    );
  }
}
