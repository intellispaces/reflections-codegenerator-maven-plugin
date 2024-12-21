package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

record SettingsImpl(
    String specificationPath,
    String packageName,
    String outputDirectory
) implements Settings {
}
