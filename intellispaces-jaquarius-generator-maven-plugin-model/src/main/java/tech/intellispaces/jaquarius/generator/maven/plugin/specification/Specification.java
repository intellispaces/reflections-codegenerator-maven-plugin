package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.nio.file.Path;

public interface Specification {

  Path path();

  SpecificationVersion version();
}
