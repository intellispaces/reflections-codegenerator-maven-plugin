package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.jaquarius.space.domain.CoreDomain;

import java.util.Map;

/**
 * The plugin settings.
 */
public interface Settings {

  String projectPath();

  String specificationPath();

  String outputDirectory();

  Map<String, CoreDomain> coreDomains();
}
