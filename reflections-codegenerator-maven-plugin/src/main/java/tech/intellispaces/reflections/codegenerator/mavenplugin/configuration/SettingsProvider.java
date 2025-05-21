package tech.intellispaces.reflections.codegenerator.mavenplugin.configuration;

public interface SettingsProvider {

  static SettingsBuilder builder() {
    return new SettingsBuilder();
  }
}
