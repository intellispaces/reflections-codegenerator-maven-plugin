package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

public class SettingsBuilder {
  private String projectPath;
  private String specificationPath;
  private String outputDirectory;
  private String basePackage;

  public SettingsBuilder projectPath(String projectPath) {
    this.projectPath = projectPath;
    return this;
  }

  public SettingsBuilder specificationPath(String specificationPath) {
    this.specificationPath = specificationPath;
    return this;
  }

  public SettingsBuilder outputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
    return this;
  }

  public SettingsBuilder basePackage(String basePackage) {
    this.basePackage = basePackage;
    return this;
  }

  public Settings get() {
    return new SettingsImpl(
        projectPath,
        specificationPath,
        outputDirectory,
        basePackage
    );
  }
}
