package tech.intellispaces.jaquarius.generator.maven.plugin.dictionary;

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

  Dictionary traverse(String path) throws MojoExecutionException;

  boolean hasProperty(String propertyName);

  String readString(String propertyName) throws MojoExecutionException;

  String readStringNullable(String propertyName) throws MojoExecutionException;

  Dictionary readDictionary(String propertyName) throws MojoExecutionException;

  List<Dictionary> readDictionaryList(String propertyName) throws MojoExecutionException;

  List<Dictionary> readDictionaryListNullable(String propertyName) throws MojoExecutionException;

  List<String> readStringList(String propertyName) throws MojoExecutionException;

  List<String> readStringListNullable(String propertyName) throws MojoExecutionException;
}
