package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationProvider;

/**
 * The plugin configuration.
 */
public interface Configuration {

  Settings settings();

  SpecificationProvider specificationProvider();

  Log log();
}
