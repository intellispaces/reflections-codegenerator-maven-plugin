package tech.intellispaces.reflections.generator.maven.plugin.specification;

import tech.intellispaces.specification.space.SpecificationItem;

import java.util.Map;

class SpecificationContextImpl implements SpecificationContext {
  private final SpecificationContext parentContext;
  private final Map<String, SpecificationItem> referenceToItemIndex;

  public SpecificationContextImpl(
      SpecificationContext parentContext,
      Map<String, SpecificationItem> referenceToItemIndex
  ) {
    this.parentContext = parentContext;
    this.referenceToItemIndex = referenceToItemIndex;
  }

  @Override
  public SpecificationItem get(String reference) {
    SpecificationItem item = referenceToItemIndex.get(reference);
    if (item == null && parentContext != null) {
      item = parentContext.get(reference);
    }
    return item;
  }
}
