package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import tech.intellispaces.core.specification.space.SpecificationItem;

public interface SpecificationContext {

  SpecificationItem get(String reference);
}
