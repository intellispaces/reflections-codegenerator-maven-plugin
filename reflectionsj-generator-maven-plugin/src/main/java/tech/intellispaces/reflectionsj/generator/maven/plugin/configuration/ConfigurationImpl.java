package tech.intellispaces.reflectionsj.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpaceRepository;

record ConfigurationImpl(
    Settings settings,
    SpaceRepository repository,
    Log log
) implements Configuration {
}
