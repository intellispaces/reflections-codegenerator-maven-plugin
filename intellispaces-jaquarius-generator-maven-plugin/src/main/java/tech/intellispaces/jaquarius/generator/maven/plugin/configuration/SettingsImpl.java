package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import java.util.Map;

record SettingsImpl(
    String specificationPath,
    String outputDirectory,
    Map<String, String> classMapping
) implements Settings {
}
