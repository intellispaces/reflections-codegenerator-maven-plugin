package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.jaquarius.space.domain.PrimaryDomainSet;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String outputDirectory,
    PrimaryDomainSet primaryDomains
) implements Settings {
}
