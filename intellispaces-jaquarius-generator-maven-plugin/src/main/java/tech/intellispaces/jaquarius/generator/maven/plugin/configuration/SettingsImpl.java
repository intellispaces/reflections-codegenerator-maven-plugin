package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.jaquarius.space.domain.CoreDomain;

import java.util.Map;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String outputDirectory,
    Map<CoreDomain, String> coreDomains
) implements Settings {
}
