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
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.ContextEquivalence;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.DomainReference;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Specification;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.ContextChannel;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.Domain;
import tech.intellispaces.jaquarius.space.domain.CoreDomains;
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

public class GenerationFunctions {

  public static void generateArtifacts(Specification spec, Configuration cfg) throws MojoExecutionException {
    cfg.log().info("Process specification " + spec.specPath());
    generateDomains(spec.ontology().domains(), cfg);
  }

  static void generateDomains(
      List<Domain> domainSpecs, Configuration cfg
  ) throws MojoExecutionException {
    try {
      Template template = Templates.get("/domain.template");
      for (Domain domainSpec : domainSpecs) {
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
      Domain domainSpec, String canonicalName, Configuration cfg
  ) throws MojoExecutionException {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Domain.class);
    return Map.of(
        "did", domainSpec.did(),
        "typeParams", buildTypeParamDeclarations(domainSpec, imports, cfg),
        "parents", buildParentsTemplateVariables(domainSpec, imports, cfg),
        "channels", buildChannelTemplateVariables(domainSpec, imports, cfg),
        "packageName", ClassNameFunctions.getPackageName(canonicalName),
        "simpleName", ClassNameFunctions.getSimpleName(canonicalName),
        "importedClasses", imports.getImports()
    );
  }

  static List<String> buildTypeParamDeclarations(
      Domain domainSpec, MutableImportList imports, Configuration cfg
  ) {
    if (domainSpec.name() != null && domainSpec.name().equals(getDomainOfDomainsName(cfg))) {
      return List.of("D");
    }
    return domainSpec.channels().stream()
        .filter(c -> isTypeRelatedChannel(c, cfg))
        .map(c -> buildTypeParamDeclaration(c, imports, cfg))
        .toList();
  }

  static List<String> buildTypeParamDeclarations(
      ContextChannel channelSpec, MutableImportList imports, Configuration cfg
  ) {
    return channelSpec.projections().stream()
        .filter(c -> isTypeRelatedChannel(c, cfg))
        .map(c -> buildTypeParamDeclaration(c, imports, cfg))
        .toList();
  }

  static String buildTypeParamDeclaration(
      ContextChannel channelSpec, MutableImportList imports, Configuration cfg
  ) {
    if (CollectionFunctions.isNullOrEmpty(channelSpec.targetDomain().superDomainBounds())) {
      return channelSpec.targetAlias();
    }

    var sb = new StringBuilder();
    sb.append(channelSpec.targetAlias());
    sb.append(" extends ");
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    for (DomainReference extendedDomain : channelSpec.targetDomain().superDomainBounds()) {
      commaAppender.run();
      sb.append(imports.addAndGetSimpleName(getDomainClassName(extendedDomain, cfg)));
    }
    return sb.toString();
  }

  static List<Map<String, Object>> buildParentsTemplateVariables(
      Domain domainSpec, MutableImportList imports, Configuration cfg
  ) throws MojoExecutionException {
    var parens = new ArrayList<Map<String, Object>>();
    for (DomainReference superDomain : domainSpec.superDomains()) {
      parens.add(Map.of(
          "name", imports.addAndGetSimpleName(getDomainClassName(superDomain, cfg)),
          "typeParams", buildParentTypeParamsDeclaration(domainSpec, superDomain, cfg)
      ));
    }
    return parens;
  }

  static String buildParentTypeParamsDeclaration(
      Domain domainSpec, DomainReference superDomainReference, Configuration cfg
  ) throws MojoExecutionException {
    Domain superDomain = cfg.specificationProvider().getDomainByName(superDomainReference.name());
    List<ContextChannel> typeRelatedChannels = getTypeRelatedChannels(superDomain, cfg);
    if (typeRelatedChannels.isEmpty()) {
      return "";
    }

    Map<String, ContextChannel> channelIndex = domainSpec.channels().stream()
        .collect(Collectors.toMap(ContextChannel::alias, Function.identity()));
    Map<String, ContextEquivalence> equivalenceIndex = superDomainReference.equivalences().stream()
        .collect(Collectors.toMap(ContextEquivalence::projectionAlias, Function.identity()));

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("<");
    for (ContextChannel typeRelatedChannel : typeRelatedChannels) {
      ContextEquivalence equivalence = equivalenceIndex.get(typeRelatedChannel.alias());
      ContextChannel channel = channelIndex.get(equivalence.matchedProjectionAlias());
      commaAppender.run();
      sb.append(channel.targetAlias());
    }
    sb.append(">");
    return sb.toString();
  }

  static List<Map<String, Object>> buildChannelTemplateVariables(
      Domain domainSpec, MutableImportList imports, Configuration cfg
  ) {
    var variables = new ArrayList<Map<String, Object>>();
    for (ContextChannel channelSpec : domainSpec.channels()) {
      var map = new HashMap<String, Object>();
      map.put("alias", channelSpec.alias());
      map.put("cid", channelSpec.cid());
      map.put("name", channelSpec.name());
      map.put("typeParams", buildTypeParamDeclarations(channelSpec, imports, cfg));
      map.put("target", buildChannelTargetDeclaration(channelSpec, imports, cfg));
      map.put("qualifiers", buildChannelQualifiers(channelSpec, imports, cfg));
      map.put("allowedTraverse", buildAllowedTraverse(channelSpec.allowedTraverse(), imports));
      variables.add(map);
    }
    if (!variables.isEmpty()) {
      imports.add(Channel.class);
    }
    return variables;
  }

  static String buildChannelTargetDeclaration(
      ContextChannel channelSpec, MutableImportList imports, Configuration cfg
  ) {
    DomainReference targetDomainReference = channelSpec.targetDomain();
    if (targetDomainReference != null && targetDomainReference.name() != null) {
      String targetDomainName = targetDomainReference.name();
      String targetDomainClass = getDomainClassName(targetDomainName, cfg);
      String className = imports.addAndGetSimpleName(targetDomainClass);
      if (!targetDomainName.equals(getDomainOfDomainsName(cfg))) {
        return className;
      }

      var sb = new StringBuilder();
      sb.append(className);
      sb.append("<");
      if (channelSpec.targetAlias() == null) {
        sb.append("?");
      } else {
        sb.append(channelSpec.targetAlias());
      }
      sb.append(">");
      return sb.toString();
    } else if (channelSpec.targetAlias() != null) {
      return channelSpec.targetAlias();
    }
    throw NotImplementedExceptions.withCode("ymDLHA");
  }

  static List<Map<String, Object>> buildChannelQualifiers(
      ContextChannel channelSpec, MutableImportList imports, Configuration cfg
  ) {
    return CollectionFunctions.mapEach(channelSpec.projections(), qs -> buildChannelValueQualifier(qs, imports, cfg));
  }

  static Map<String, Object> buildChannelValueQualifier(
      ContextChannel qualifierChannel, MutableImportList imports, Configuration cfg
  ) {
    var map = new HashMap<String, Object>();
    map.put("name", qualifierChannel.targetAlias());
    map.put("class", imports.addAndGetSimpleName(getDomainClassName(qualifierChannel.targetDomain().name(), cfg)));
    return map;
  }

  static boolean isTypeRelatedChannel(ContextChannel channelSpec, Configuration cfg) {
    String domainDomainName = cfg.settings().coreDomains().get(CoreDomains.Domain);
    return domainDomainName.equals(channelSpec.targetDomain().name());
  }

  static List<ContextChannel> getTypeRelatedChannels(Domain domainSpec, Configuration cfg) {
    return domainSpec.channels().stream()
        .filter(c -> isTypeRelatedChannel(c, cfg))
        .toList();
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

  static String getDomainClassName(Domain domainSpec, Configuration cfg) {
    return getClassName(domainSpec.name() + "Domain", cfg);
  }

  static String getDomainClassName(DomainReference domainReference, Configuration cfg) {
    return getDomainClassName(domainReference.name(), cfg);
  }

  static String getDomainClassName(String domainName, Configuration cfg) {
    return getClassName(domainName + "Domain", cfg);
  }

  static String getClassName(String entityName, Configuration cfg) {
    return entityName.replaceFirst("intellispaces\\.", "tech.intellispaces.jaquarius.");
  }

  static String getDomainOfDomainsName(Configuration cfg) {
    String name = cfg.settings().coreDomains().get(CoreDomains.Domain);
    if (name == null) {
      throw UnexpectedExceptions.withMessage("Name of the domain of domains is not defined");
    }
    return name;
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

  private GenerationFunctions() {}
}
