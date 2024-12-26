package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;

/**
 * The plugin configuration.
 */
public interface Configuration {

  Log log();

  Settings settings();
}
