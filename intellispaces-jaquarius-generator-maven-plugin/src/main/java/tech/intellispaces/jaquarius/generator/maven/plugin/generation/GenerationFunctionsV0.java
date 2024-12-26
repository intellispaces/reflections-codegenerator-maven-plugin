package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.action.runnable.RunnableAction;
import tech.intellispaces.action.text.StringActions;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.text.StringFunctions;
import tech.intellispaces.general.type.ClassNameFunctions;
import tech.intellispaces.jaquarius.annotation.Channel;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.DomainPurpose;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.ChannelQualifiedSpecification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.DomainChannelSpecification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.DomainSpecification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.ParentDomainSpecification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.SpecificationV0;
import tech.intellispaces.jaquarius.traverse.TraverseTypes;
import tech.intellispaces.java.reflection.customtype.ImportLists;
import tech.intellispaces.java.reflection.customtype.MutableImportList;
import tech.intellispaces.templateengine.exception.ResolveTemplateException;
import tech.intellispaces.templateengine.template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface GenerationFunctionsV0 {

  static void generateArtifacts(SpecificationV0 spec, Configuration cfg) throws MojoExecutionException {
    generateDomains(spec.ontology().domains(), cfg);
  }

  private static void generateDomains(
      List<DomainSpecification> domainSpecs, Configuration cfg
  ) throws MojoExecutionException {
    try {
      Template template = Templates.get("/domain.template");
      for (DomainSpecification domainSpec : domainSpecs) {
        String canonicalName = getDomainClassName(domainSpec, cfg);
        Map<String, Object> templateVars = buildDomainTemplateVariables(domainSpec, canonicalName, cfg);
        String source = template.resolve(templateVars);
        write(cfg, canonicalName, source);
      }
    } catch (ResolveTemplateException e) {
      throw new MojoExecutionException("Could not generate domain class", e);
    }
  }

  private static Map<String, Object> buildDomainTemplateVariables(
      DomainSpecification domainSpec, String canonicalName, Configuration cfg
  ) {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Domain.class);
    return Map.of(
        "did", domainSpec.did(),
        "parents", buildParentsTemplateVariables(domainSpec.parents(), imports, cfg),
        "channels", buildDomainChannelTemplateVariables(domainSpec.channels(), imports, cfg),
        "packageName", ClassNameFunctions.getPackageName(canonicalName),
        "simpleName", ClassNameFunctions.getSimpleName(canonicalName),
        "importedClasses", imports.getImports()
    );
  }

  private static List<Map<String, Object>> buildParentsTemplateVariables(
      List<ParentDomainSpecification> parentSpecs, MutableImportList imports, Configuration cfg
  ) {
    var parens = new ArrayList<Map<String, Object>>();
    for (ParentDomainSpecification parentSpec : parentSpecs) {
      parens.add(Map.of(
          "name", imports.addAndGetSimpleName(getDomainClassName(parentSpec, cfg))
      ));
    }
    return parens;
  }

  private static List<Map<String, Object>> buildDomainChannelTemplateVariables(
      List<DomainChannelSpecification> channelSpecs, MutableImportList imports, Configuration cfg
  ) {
    var channels = new ArrayList<Map<String, Object>>();
    for (DomainChannelSpecification channelSpec : channelSpecs) {
      var map = new HashMap<String, Object>();
      map.put("target", imports.addAndGetSimpleName(getTargetDomainClassName(channelSpec, cfg)));
      map.put("alias", channelSpec.alias());
      map.put("cid", channelSpec.cid());
      map.put("name", channelSpec.name());
      map.put("allowedTraverse", buildAllowedTraverse(channelSpec.allowedTraverse(), imports));
      map.put("qualifiers", buildChannelQualifiers(channelSpec.qualifiers(), imports, cfg));
      channels.add(map);
    }
    if (!channels.isEmpty()) {
      imports.add(Channel.class);
    }
    return channels;
  }

  private static List<Map<String, Object>> buildChannelQualifiers(
      List<ChannelQualifiedSpecification> qualifiers, MutableImportList imports, Configuration cfg
  ) {
    return CollectionFunctions.mapEach(qualifiers, q -> buildChannelQualifier(q, imports, cfg));
  }

  private static Map<String, Object> buildChannelQualifier(
      ChannelQualifiedSpecification qualifierSpec, MutableImportList imports, Configuration cfg
  ) {
    var map = new HashMap<String, Object>();
    map.put("name", qualifierSpec.name());
    map.put("class", imports.addAndGetSimpleName(getDomainClassName(qualifierSpec.domainName(), cfg)));
    return map;
  }

  private static String buildAllowedTraverse(List<String> allowedTraverses, MutableImportList imports) {
    if (allowedTraverses == null) {
      return null;
    }
    if (allowedTraverses.size() == 1) {
      return imports.addAndGetSimpleName(TraverseTypes.class) + "." +
          StringFunctions.capitalizeFirstLetter(allowedTraverses.get(0));
    }

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("{");
    for (String allowedTraverse : allowedTraverses) {
      commaAppender.run();
      sb.append(imports.addAndGetSimpleName(TraverseTypes.class))
          .append(".")
          .append(StringFunctions.capitalizeFirstLetter(allowedTraverse));
    }
    sb.append("}");
    return sb.toString();
  }

  private static String getDomainClassName(DomainSpecification domainSpec, Configuration cfg) {
    return getDomainClassName(domainSpec.label(), cfg);
  }

  private static String getDomainClassName(ParentDomainSpecification parentDomainSpec, Configuration cfg) {
    return getDomainClassName(parentDomainSpec.label(), cfg);
  }

  private static String getTargetDomainClassName(DomainChannelSpecification channelSpec, Configuration cfg) {
    return getDomainClassName(channelSpec.targetDomainName(), cfg);
  }

  private static String getDomainClassName(String domainName, Configuration cfg) {
    DomainPurpose domainPurpose = cfg.settings().domainPurposes().get(domainName);
    if (domainPurpose != null) {
      return domainPurpose.className();
    }
    return getClassName(domainName + "Domain", cfg);
  }

  private static String getClassName(String entityName, Configuration cfg) {
    return entityName.replaceFirst("intellispaces\\.", "tech.intellispaces.jaquarius.");
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
