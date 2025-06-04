package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

import org.apache.maven.plugin.logging.Log;

import tech.intellispaces.specification.space.repository.SpecificationRepository;

record ConfigurationImpl(
    Settings settings,
    SpecificationRepository repository,
    Log log
) implements Configuration {
}
