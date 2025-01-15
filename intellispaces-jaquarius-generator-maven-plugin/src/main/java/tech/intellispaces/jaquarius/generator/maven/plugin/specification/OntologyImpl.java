package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

record OntologyImpl(
    List<Domain> domains
) implements Ontology {
}
