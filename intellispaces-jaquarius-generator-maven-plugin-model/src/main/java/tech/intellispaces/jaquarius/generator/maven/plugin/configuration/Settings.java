package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import java.util.Map;

/**
 * The plugin settings.
 */
public interface Settings {

  String specificationPath();

  String outputDirectory();

  Map<String, String> classMapping();
}
