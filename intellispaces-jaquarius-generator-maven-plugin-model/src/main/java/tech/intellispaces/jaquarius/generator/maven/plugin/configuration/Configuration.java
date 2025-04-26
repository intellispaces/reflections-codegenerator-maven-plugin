package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpaceRepository;

/**
 * The plugin configuration.
 */
public interface Configuration {

  Settings settings();

  SpaceRepository repository();

  Log log();
}
