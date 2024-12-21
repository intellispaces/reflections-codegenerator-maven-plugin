package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.general.text.StringFunctions;
import tech.intellispaces.general.type.ClassNameFunctions;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.DomainSpecificationV0;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0;
import tech.intellispaces.templateengine.exception.ResolveTemplateException;
import tech.intellispaces.templateengine.template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface GenerationV0Functions {

  static void generate(Configuration cfg, SpecificationV0 spec) throws MojoExecutionException {

    generateDomains(cfg, spec.ontology().domains());

  }

  private static void generateDomains(
      Configuration cfg, List<DomainSpecificationV0> domainSpecs
  ) throws MojoExecutionException {
    try {
      Template template = Templates.get("/domain.template");
      for (DomainSpecificationV0 domainSpec : domainSpecs) {

        String canonicalName = getDomainCanonicalName(cfg, domainSpec);
        Map<String, Object> templateVars = domainTemplateVariables(cfg, domainSpec, canonicalName);
        String source = template.resolve(templateVars);
        write(cfg, canonicalName, source);
      }
    } catch (ResolveTemplateException e) {
      throw new MojoExecutionException("Could not generate domain class", e);
    }
  }

  private static Map<String, Object> domainTemplateVariables(
      Configuration cfg, DomainSpecificationV0 domainSpec, String canonicalName
  ) {
    return Map.of(
        "packageName", ClassNameFunctions.getPackageName(canonicalName),
        "simpleName", ClassNameFunctions.getSimpleName(canonicalName),
        "did", domainSpec.did()
    );
  }

  private static String getDomainCanonicalName(Configuration cfg, DomainSpecificationV0 domainSpec) {
    String fullName = domainSpec.label() + "Domain";
    return StringFunctions.join(cfg.settings().packageName(), fullName, ".");
  }

  private static void write(
      Configuration cfg, String canonicalName, String source
  ) throws MojoExecutionException {
    Path path = new File(StringFunctions.join(
        cfg.settings().outputDirectory(),
        canonicalName.replace(".", File.separator),
        File.separator
    ) + ".java").toPath();
    try {
      Files.createDirectories(path.getParent());
      Files.writeString(path, source);
    } catch (IOException e) {
      throw new MojoExecutionException("Could not write file " + path, e);
    }
  }
}
