package tech.intellispaces.reflections.generator.maven.plugin.configuration;

public interface SettingsProvider {

  static SettingsBuilder builder() {
    return new SettingsBuilder();
  }
}
