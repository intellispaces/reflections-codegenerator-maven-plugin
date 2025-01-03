package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

public record ValueQualifierSpecificationImpl(
    String name,
    DomainReference domain
) implements ValueQualifierSpecification {
}
