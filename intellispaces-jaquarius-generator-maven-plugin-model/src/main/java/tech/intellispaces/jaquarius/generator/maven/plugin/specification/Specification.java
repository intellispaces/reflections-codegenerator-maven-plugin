package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.nio.file.Path;

/**
 * The IntelliSpaces specification.
 */
public interface Specification {

  Path specPath();

  Ontology ontology();
}
