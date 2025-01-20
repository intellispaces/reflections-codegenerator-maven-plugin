package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public interface Values {

  static Value get(String stringValue) {
    return new ValueImpl(stringValue);
  }
}
