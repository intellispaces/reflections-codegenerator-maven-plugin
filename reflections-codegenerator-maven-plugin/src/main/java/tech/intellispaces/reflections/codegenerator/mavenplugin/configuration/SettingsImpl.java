package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

record SettingsImpl(
    String projectPath,
    String specificationPath,
    String basePackage,
    String generatedSourcesDirectory,
    String generatedResourcesDirectory
) implements Settings {
}
