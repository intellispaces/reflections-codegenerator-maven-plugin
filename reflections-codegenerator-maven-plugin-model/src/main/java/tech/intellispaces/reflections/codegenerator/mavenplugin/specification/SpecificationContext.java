package tech.intellispaces.reflections.codegenerator.mavenplugin.specification;

import tech.intellispaces.specification.space.SpecificationItem;

public interface SpecificationContext {

  SpecificationItem get(String reference);
}
