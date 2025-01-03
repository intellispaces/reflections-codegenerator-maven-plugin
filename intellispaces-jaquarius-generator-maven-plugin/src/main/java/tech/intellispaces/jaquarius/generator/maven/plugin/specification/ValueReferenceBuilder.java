package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public class ValueReferenceBuilder {
  private String alias;

  public ValueReferenceBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public ValueReference get() {
    return new ValueReferenceImpl(
        alias
    );
  }
}
