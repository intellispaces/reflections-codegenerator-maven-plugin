package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String outputDirectory
) implements Settings {
}
