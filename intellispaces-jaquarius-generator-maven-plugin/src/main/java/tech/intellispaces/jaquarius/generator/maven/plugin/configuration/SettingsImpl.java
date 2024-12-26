package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import java.util.Map;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String outputDirectory,
    Map<String, DomainPurpose> domainPurposes
) implements Settings {
}
