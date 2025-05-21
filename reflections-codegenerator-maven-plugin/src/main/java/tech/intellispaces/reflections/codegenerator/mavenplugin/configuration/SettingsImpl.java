package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String outputDirectory
) implements Settings {
}
