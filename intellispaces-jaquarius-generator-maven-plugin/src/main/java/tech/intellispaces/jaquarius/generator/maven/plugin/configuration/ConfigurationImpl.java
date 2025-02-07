package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import org.apache.maven.plugin.logging.Log;
import tech.intellispaces.core.specification.repository.OntologyRepository;

record ConfigurationImpl(
    Settings settings,
    OntologyRepository repository,
    Log log
) implements Configuration {
}
