package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

public class SettingsBuilder {
  private String specificationPath;
  private String packageName;
  private String outputDirectory;

  public SettingsBuilder specificationPath(String inputSpec) {
    this.specificationPath = inputSpec;
    return this;
  }

  public SettingsBuilder packageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public SettingsBuilder outputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
    return this;
  }

  public Settings get() {
    return new SettingsImpl(
        specificationPath,
        packageName,
        outputDirectory
    );
  }
}
