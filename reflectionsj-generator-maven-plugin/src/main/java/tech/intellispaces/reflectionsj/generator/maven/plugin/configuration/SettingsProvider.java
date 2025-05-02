package tech.intellispaces.reflectionsj.generator.maven.plugin.configuration;

public interface SettingsProvider {

  static SettingsBuilder builder() {
    return new SettingsBuilder();
  }
}
