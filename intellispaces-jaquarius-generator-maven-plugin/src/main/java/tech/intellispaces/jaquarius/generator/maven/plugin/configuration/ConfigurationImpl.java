package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;
import tech.intellispaces.core.specification.space.repository.SpaceRepository;

record ConfigurationImpl(
    Settings settings,
    SpaceRepository repository,
    Log log
) implements Configuration {
}
