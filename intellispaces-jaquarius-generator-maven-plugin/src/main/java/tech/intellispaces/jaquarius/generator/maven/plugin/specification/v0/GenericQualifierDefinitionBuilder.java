package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

public class GenericQualifierDefinitionBuilder {
  private String alias;
  private String valueReference;

  public GenericQualifierDefinitionBuilder alias(String alias) {
    this.alias = alias;
    return this;
  }

  public GenericQualifierDefinitionBuilder valueReference(String valueReference) {
    this.valueReference = valueReference;
    return this;
  }

  public GenericQualifierDefinition get() {
    return new GenericQualifierDefinitionImpl(
        alias,
        valueReference
    );
  }
}
