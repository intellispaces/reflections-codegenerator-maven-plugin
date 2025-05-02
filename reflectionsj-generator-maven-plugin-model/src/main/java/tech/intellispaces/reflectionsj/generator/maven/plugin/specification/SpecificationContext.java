package tech.intellispaces.reflectionsj.generator.maven.plugin.specification;

import tech.intellispaces.specification.space.SpecificationItem;

public interface SpecificationContext {

  SpecificationItem get(String reference);
}
