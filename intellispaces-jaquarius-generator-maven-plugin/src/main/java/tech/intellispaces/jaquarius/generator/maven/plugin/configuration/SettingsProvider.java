package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

public interface SettingsProvider {

  static SettingsBuilder builder() {
    return new SettingsBuilder();
  }
}
