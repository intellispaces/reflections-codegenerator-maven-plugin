package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;

record ConfigurationImpl(
    Log log,
    Settings settings
) implements Configuration {
}
