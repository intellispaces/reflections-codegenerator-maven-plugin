package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.nio.file.Path;

public interface Specifications {

  static SpecificationBuilder build(Path path) {
    return new SpecificationBuilder(path);
  }
}
