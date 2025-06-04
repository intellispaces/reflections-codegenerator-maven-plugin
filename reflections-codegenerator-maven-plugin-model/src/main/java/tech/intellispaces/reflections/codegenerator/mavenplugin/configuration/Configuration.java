package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpecificationRepository;

/**
 * The plugin configuration.
 */
public interface Configuration {

  Settings settings();

  SpecificationRepository repository();

  Log log();
}
