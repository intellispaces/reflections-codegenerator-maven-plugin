package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.jaquarius.space.domain.CoreDomain;

import java.util.Map;

public class SettingsBuilder {
  private String projectPath;
  private String specificationPath;
  private String outputDirectory;
  private Map<String, CoreDomain> coreDomains = Map.of();

  public SettingsBuilder projectPath(String projectPath) {
    this.projectPath = projectPath;
    return this;
  }

  public SettingsBuilder specificationPath(String inputSpec) {
    this.specificationPath = inputSpec;
    return this;
  }

  public SettingsBuilder outputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
    return this;
  }

  public SettingsBuilder coreDomains(Map<String, CoreDomain> coreDomains) {
    this.coreDomains = coreDomains;
    return this;
  }

  public Settings get() {
    return new SettingsImpl(
        projectPath,
        specificationPath,
        outputDirectory,
        coreDomains
    );
  }
}
