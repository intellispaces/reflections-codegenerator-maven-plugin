package tech.intellispaces.reflectionsj.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpaceRepository;

public class ConfigurationBuilder {
  private Settings settings;
  private SpaceRepository repository;
  private Log log;

  public ConfigurationBuilder settings(Settings settings) {
    this.settings = settings;
    return this;
  }

  public ConfigurationBuilder log(Log log) {
    this.log = log;
    return this;
  }

  public ConfigurationBuilder repository(SpaceRepository repository) {
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
