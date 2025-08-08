package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

public class SettingsBuilder {
  private String projectPath;
  private String specificationPath;
  private String basePackage;
  private String generatedSourcesDirectory;
  private String generatedResourcesDirectory;

  public SettingsBuilder projectPath(String projectPath) {
    this.projectPath = projectPath;
    return this;
  }

  public SettingsBuilder specificationPath(String specificationPath) {
    this.specificationPath = specificationPath;
    return this;
  }

  public SettingsBuilder basePackage(String basePackage) {
    this.basePackage = basePackage;
    return this;
  }

  public SettingsBuilder generatedSourcesDirectory(String generatedSourcesDirectory) {
    this.generatedSourcesDirectory = generatedSourcesDirectory;
    return this;
  }

  public SettingsBuilder generatedResourcesDirectory(String generatedResourcesDirectory) {
    this.generatedResourcesDirectory = generatedResourcesDirectory;
    return this;
  }

  public Settings get() {
    return new SettingsImpl(
        projectPath,
        specificationPath,
        basePackage,
        generatedSourcesDirectory,
        generatedResourcesDirectory
    );
  }
}
