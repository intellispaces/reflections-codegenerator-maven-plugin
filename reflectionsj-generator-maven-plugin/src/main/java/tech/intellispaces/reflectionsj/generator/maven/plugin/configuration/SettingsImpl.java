package tech.intellispaces.reflectionsj.generator.maven.plugin.configuration;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String outputDirectory
) implements Settings {
}
