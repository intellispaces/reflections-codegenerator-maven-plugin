package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.action.runnable.RunnableAction;
import tech.intellispaces.action.text.StringActions;
import tech.intellispaces.general.collection.CollectionFunctions;
import tech.intellispaces.general.exception.NotImplementedExceptions;
import tech.intellispaces.general.exception.UnexpectedExceptions;
import tech.intellispaces.general.text.StringFunctions;
import tech.intellispaces.general.type.ClassNameFunctions;
import tech.intellispaces.jaquarius.annotation.Channel;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.DomainPurpose;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.DomainPurposes;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.GenericQualifierDefinition;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.GenericQualifierSpecification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0.ValueQualifiedSpecification;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerationFunctionsV0 {

  public static void generateArtifacts(SpecificationV0 spec, Configuration cfg) throws MojoExecutionException {
    generateDomains(spec.ontology().domains(), cfg);
  }

  static void generateDomains(
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

  static Map<String, Object> buildDomainTemplateVariables(
      DomainSpecification domainSpec, String canonicalName, Configuration cfg
  ) {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Domain.class);
    return Map.of(
        "did", domainSpec.did(),
        "genericQualifiers", buildGenericQualifierDeclarations(domainSpec.genericQualifiers(), imports, cfg),
        "parents", buildParentsTemplateVariables(domainSpec.parents(), imports, cfg),
        "channels", buildDomainChannelTemplateVariables(domainSpec.channels(), imports, cfg),
        "packageName", ClassNameFunctions.getPackageName(canonicalName),
        "simpleName", ClassNameFunctions.getSimpleName(canonicalName),
        "importedClasses", imports.getImports()
    );
  }

  static List<String> buildGenericQualifierDeclarations(
      List<GenericQualifierSpecification> qualifierSpecs, MutableImportList imports, Configuration cfg
  ) {
    return qualifierSpecs.stream()
        .map(GenericQualifierSpecification::name)
        .toList();
  }

  static List<Map<String, Object>> buildParentsTemplateVariables(
      List<ParentDomainSpecification> parentSpecs, MutableImportList imports, Configuration cfg
  ) {
    var parens = new ArrayList<Map<String, Object>>();
    for (ParentDomainSpecification parentSpec : parentSpecs) {
      parens.add(Map.of(
          "name", imports.addAndGetSimpleName(getDomainClassName(parentSpec, cfg)),
          "typeParams", buildParentTypeParamsDeclaration(parentSpec, cfg)
      ));
    }
    return parens;
  }

  static String buildParentTypeParamsDeclaration(
      ParentDomainSpecification parentSpec, Configuration cfg
  ) {
    DomainSpecification parentDomain = cfg.specificationProvider().domainV0ByName(parentSpec.label());
    if (parentDomain.genericQualifiers().isEmpty()) {
      return "";
    }

    Map<String, GenericQualifierDefinition> index = parentSpec.genericQualifierDefinitions().stream()
        .collect(Collectors.toMap(GenericQualifierDefinition::alias, Function.identity()));

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("<");
    for (GenericQualifierSpecification genericParam : parentDomain.genericQualifiers()) {
      String typeParamName = genericParam.name();
      GenericQualifierDefinition typeParamDefinition = index.get(typeParamName);
      commaAppender.run();
      sb.append(typeParamDefinition.valueReference());
    }
    sb.append(">");
    return sb.toString();
  }

  static List<Map<String, Object>> buildDomainChannelTemplateVariables(
      List<DomainChannelSpecification> channelSpecs, MutableImportList imports, Configuration cfg
  ) {
    var channels = new ArrayList<Map<String, Object>>();
    for (DomainChannelSpecification channelSpec : channelSpecs) {
      var map = new HashMap<String, Object>();
      map.put("target", buildChannelTargetDeclaration(channelSpec, imports, cfg));
      map.put("genericQualifiers", buildChannelTargetTypeParams(channelSpec, imports, cfg));
      map.put("alias", channelSpec.alias());
      map.put("cid", channelSpec.cid());
      map.put("name", channelSpec.name());
      map.put("allowedTraverse", buildAllowedTraverse(channelSpec.allowedTraverse(), imports));
      map.put("valueQualifiers", buildChannelValueQualifiers(channelSpec.valueQualifiers(), imports, cfg));
      channels.add(map);
    }
    if (!channels.isEmpty()) {
      imports.add(Channel.class);
    }
    return channels;
  }

  static String buildChannelTargetDeclaration(
      DomainChannelSpecification channelSpec, MutableImportList imports, Configuration cfg
  ) {
    if (channelSpec.targetDomainName() != null) {
      String targetDomainName = channelSpec.targetDomainName();
      String targetDomainClass = getDomainClassName(channelSpec.targetDomainName(), cfg);
      String className = imports.addAndGetSimpleName(targetDomainClass);
      if (!targetDomainName.equals(getDomainDomainName(cfg))) {
        return className;
      }

      var sb = new StringBuilder();
      sb.append(className);
      sb.append("<");
      sb.append(channelSpec.targetValueRef());
      sb.append(">");
      return sb.toString();
    } else if (channelSpec.targetDomainRef() != null) {
      return channelSpec.targetDomainRef();
    }
    throw NotImplementedExceptions.withCode("ymDLHA");
  }

  static List<String> buildChannelTargetTypeParams(
      DomainChannelSpecification channelSpec, MutableImportList imports, Configuration cfg
  ) {
    if (channelSpec.targetDomainGenericQualifierDefinitions().isEmpty()) {
      return List.of();
    }

    Map<String, GenericQualifierDefinition> index = channelSpec.targetDomainGenericQualifierDefinitions().stream()
        .collect(Collectors.toMap(GenericQualifierDefinition::alias, Function.identity()));

    List<String> result = new ArrayList<>();
    DomainSpecification targetDomain = cfg.specificationProvider().domainV0ByName(channelSpec.targetDomainName());
    for (GenericQualifierSpecification genericParam : targetDomain.genericQualifiers()) {
      String typeParamName = genericParam.name();
      GenericQualifierDefinition typeParamDefinition = index.get(typeParamName);

      result.add(typeParamDefinition.valueReference());
    }
    return result;
  }

  static List<Map<String, Object>> buildChannelValueQualifiers(
      List<ValueQualifiedSpecification> qualifierSpecs, MutableImportList imports, Configuration cfg
  ) {
    return CollectionFunctions.mapEach(qualifierSpecs, qs -> buildChannelValueQualifier(qs, imports, cfg));
  }

  static Map<String, Object> buildChannelValueQualifier(
      ValueQualifiedSpecification qualifierSpec, MutableImportList imports, Configuration cfg
  ) {
    var map = new HashMap<String, Object>();
    map.put("name", qualifierSpec.name());
    map.put("class", imports.addAndGetSimpleName(getDomainClassName(qualifierSpec.domainName(), cfg)));
    return map;
  }

  static String buildAllowedTraverse(List<String> allowedTraverses, MutableImportList imports) {
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

  static String getDomainClassName(DomainSpecification domainSpec, Configuration cfg) {
    return getClassName(domainSpec.label() + "Domain", cfg);
  }

  static String getDomainClassName(ParentDomainSpecification parentSpec, Configuration cfg) {
    return getDomainClassName(parentSpec.label(), cfg);
  }

  static String getDomainClassName(String domainName, Configuration cfg) {
    DomainPurpose domainPurpose = cfg.settings().domainPurposes().get(domainName);
    if (domainPurpose != null) {
      return domainPurpose.className();
    }
    return getClassName(domainName + "Domain", cfg);
  }

  static String getClassName(String entityName, Configuration cfg) {
    return entityName.replaceFirst("intellispaces\\.", "tech.intellispaces.jaquarius.");
  }

  static String getDomainDomainName(Configuration cfg) {
    return cfg.settings().domainPurposes().entrySet().stream()
        .filter(e -> DomainPurposes.Domain.is(e.getValue()))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElseThrow(() -> UnexpectedExceptions.withMessage("Name of the domain domain is not defined"));
  }

  static void write(
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

  private GenerationFunctionsV0() {}
}
