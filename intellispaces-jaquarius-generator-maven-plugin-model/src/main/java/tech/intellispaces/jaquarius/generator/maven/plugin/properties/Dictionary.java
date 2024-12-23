package tech.intellispaces.jaquarius.generator.maven.plugin.properties;

import org.apache.maven.plugin.MojoExecutionException;

import java.util.List;

/**
 * The dictionary.
 */
public interface Dictionary {

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

  boolean hasProperty(String propertyName);

  String readString(String propertyName) throws MojoExecutionException;

  String readStringNullable(String propertyName) throws MojoExecutionException;

  Dictionary readProperties(String propertyName) throws MojoExecutionException;

  List<Dictionary> readLabeledPropertiesList(String propertyName) throws MojoExecutionException;
}
