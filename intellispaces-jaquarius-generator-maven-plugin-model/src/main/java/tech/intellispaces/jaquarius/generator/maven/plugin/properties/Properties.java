package tech.intellispaces.jaquarius.generator.maven.plugin.properties;

import org.apache.maven.plugin.MojoExecutionException;

import java.util.List;

/**
 * The properties.
 */
public interface Properties {

  /**
   * The path.
   * <p>
   * The <code>nul</code> for root properties.
   */
  String path();

  /**
   * The name.
   * <p>
   * The <code>nul</code> for root properties.
   */
  String name();

  String readString(String propertyName) throws MojoExecutionException;

  String readStringNullable(String propertyName) throws MojoExecutionException;

  Properties readProperties(String propertyName) throws MojoExecutionException;

  List<Properties> readLabeledPropertiesList(String propertyName) throws MojoExecutionException;
}
