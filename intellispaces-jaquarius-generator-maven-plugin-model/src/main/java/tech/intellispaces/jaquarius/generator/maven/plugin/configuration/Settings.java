package tech.intellispaces.jaquarius.generator.maven.plugin.configuration;

import tech.intellispaces.jaquarius.space.domain.PrimaryDomainSet;

/**
 * The plugin settings.
 */
public interface Settings {

  String projectPath();

  String specificationPath();

  String outputDirectory();

  PrimaryDomainSet primaryDomains();
}
