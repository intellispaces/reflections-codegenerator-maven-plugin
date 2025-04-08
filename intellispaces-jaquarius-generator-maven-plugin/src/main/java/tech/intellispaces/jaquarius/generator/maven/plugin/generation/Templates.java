package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import tech.intellispaces.commons.exception.UnexpectedExceptions;
import tech.intellispaces.commons.function.Functions;
import tech.intellispaces.commons.resource.ResourceFunctions;
import tech.intellispaces.templateengine.TemplateEngine;
import tech.intellispaces.templateengine.template.Template;

import java.util.HashMap;
import java.util.Map;

public class Templates {
  private static final Map<String, Template> TEMPLATE_CACHE = new HashMap<>();

  public static Template get(String templateName) {
    return TEMPLATE_CACHE.computeIfAbsent(templateName,
        Functions.wrapThrowingFunction(Templates::makeTemplate)
    );
  }

  private static Template makeTemplate(String templateName) throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(
        Templates.class, templateName
    ).orElseThrow(() -> UnexpectedExceptions.withMessage("Artifact template {0} is not found", templateName));
    return TemplateEngine.parseTemplate(templateSource);
  }

  private Templates() {}
}
