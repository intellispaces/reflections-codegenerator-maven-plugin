package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import java.util.Map;

public class SettingsBuilder {
  private String specificationPath;
  private String outputDirectory;
  private Map<String, String> classMapping = Map.of();

  public SettingsBuilder specificationPath(String inputSpec) {
    this.specificationPath = inputSpec;
    return this;
  }

  public SettingsBuilder outputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
    return this;
  }

  public SettingsBuilder classMapping(Map<String, String> classMapping) {
    this.classMapping = classMapping;
    return this;
  }

  public Settings get() {
    return new SettingsImpl(
        specificationPath,
        outputDirectory,
        classMapping
    );
  }
}
