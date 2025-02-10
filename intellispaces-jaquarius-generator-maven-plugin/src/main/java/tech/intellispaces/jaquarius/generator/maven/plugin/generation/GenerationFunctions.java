package tech.intellispaces.jaquarius.generator.maven.plugin.generation;

import org.apache.maven.plugin.MojoExecutionException;
import tech.intellispaces.commons.action.runnable.RunnableAction;
import tech.intellispaces.commons.action.text.StringActions;
import tech.intellispaces.commons.base.collection.CollectionFunctions;
import tech.intellispaces.commons.base.exception.NotImplementedExceptions;
import tech.intellispaces.commons.base.text.StringFunctions;
import tech.intellispaces.commons.base.type.ClassNameFunctions;
import tech.intellispaces.commons.java.reflection.customtype.ImportLists;
import tech.intellispaces.commons.java.reflection.customtype.MutableImportList;
import tech.intellispaces.commons.templateengine.template.Template;
import tech.intellispaces.core.specification.ContextChannelSpecification;
import tech.intellispaces.core.specification.DomainSpecification;
import tech.intellispaces.core.specification.ImmobilityTypes;
import tech.intellispaces.core.specification.Specification;
import tech.intellispaces.core.specification.SuperDomainSpecification;
import tech.intellispaces.core.specification.constraint.ConstraintSpecification;
import tech.intellispaces.core.specification.constraint.EquivalenceConstraintSpecification;
import tech.intellispaces.core.specification.exception.SpecificationException;
import tech.intellispaces.core.specification.exception.TraversePathSpecificationException;
import tech.intellispaces.core.specification.instance.CustomInstanceSpecification;
import tech.intellispaces.core.specification.instance.InstanceSpecification;
import tech.intellispaces.core.specification.reference.SpaceReference;
import tech.intellispaces.core.specification.reference.SpaceReferences;
import tech.intellispaces.core.specification.traverse.TraversePathParseFunctions;
import tech.intellispaces.core.specification.traverse.TraversePathSpecification;
import tech.intellispaces.core.specification.traverse.TraverseTransitionSpecification;
import tech.intellispaces.core.specification.traverse.TraverseTransitionThruSpecification;
import tech.intellispaces.jaquarius.annotation.Channel;
import tech.intellispaces.jaquarius.annotation.Unmovable;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.naming.NameConventionFunctions;
import tech.intellispaces.jaquarius.space.domain.BasicDomain;
import tech.intellispaces.jaquarius.space.domain.BasicDomainPurposes;
import tech.intellispaces.jaquarius.space.domain.BasicDomains;
import tech.intellispaces.jaquarius.traverse.TraverseTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerationFunctions {

  public static void generateArtifacts(Specification spec, Configuration cfg) throws MojoExecutionException {
    cfg.log().info("Process specification " + spec.specPath());
    generateDomains(spec.ontology().domains(), cfg);
  }

  static void generateDomains(
      List<DomainSpecification> domainSpecs, Configuration cfg
  ) throws MojoExecutionException {
    Template template = Templates.get("/domain.template");
    for (DomainSpecification domainSpec : domainSpecs) {
      try {
        String canonicalName = getDomainClassName(domainSpec);
        Map<String, Object> templateVars = buildDomainTemplateVariables(domainSpec, canonicalName, cfg);
        String source = template.resolve(templateVars);
        write(cfg, canonicalName, source);
      } catch (Exception e) {
        throw new MojoExecutionException("Could not process domain specification " + domainSpec.name(), e);
      }
    }
  }

  static Map<String, Object> buildDomainTemplateVariables(
      DomainSpecification domainSpec, String canonicalName, Configuration cfg
  ) throws MojoExecutionException {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Domain.class);
    return Map.of(
        "did", domainSpec.did(),
        "typeParams", buildTypeParamDeclarations(domainSpec, imports),
        "parents", buildParentsTemplateVariables(domainSpec, imports, cfg),
        "channels", buildChannelTemplateVariables(domainSpec, imports, cfg),
        "packageName", ClassNameFunctions.getPackageName(canonicalName),
        "simpleName", ClassNameFunctions.getSimpleName(canonicalName),
        "importedClasses", imports.getImports()
    );
  }

  static List<String> buildTypeParamDeclarations(
      DomainSpecification domainSpec, MutableImportList imports
  ) {
    if (domainSpec.name() != null && BasicDomains.active().isDomainDomain(domainSpec.name())) {
      return List.of("D");
    }
    return domainSpec.channels().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
        .map(c -> buildTypeParamDeclaration(c, imports))
        .toList();
  }

  static List<String> buildTypeParamDeclarations(
      ContextChannelSpecification channelSpec, MutableImportList imports
  ) {
    return channelSpec.projections().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
        .map(c -> buildTypeParamDeclaration(c, imports))
        .toList();
  }

  static String buildTypeParamDeclaration(
      ContextChannelSpecification channelSpec, MutableImportList imports
  ) {
    if (channelSpec.targetDomainBounds() == null) {
      return channelSpec.targetAlias();
    }

    var sb = new StringBuilder();
    sb.append(channelSpec.targetAlias());
    sb.append(" extends ");
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    for (SpaceReference extendedDomain : channelSpec.targetDomainBounds().superDomains()) {
      commaAppender.run();
      sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(extendedDomain)));
    }
    return sb.toString();
  }

  static List<Map<String, Object>> buildParentsTemplateVariables(
      DomainSpecification domainSpec, MutableImportList imports, Configuration cfg
  ) throws MojoExecutionException {
    var parens = new ArrayList<Map<String, Object>>();
    for (SuperDomainSpecification superDomain : domainSpec.superDomains()) {
      parens.add(Map.of(
          "name", imports.addAndGetSimpleName(getDomainClassName(superDomain.reference())),
          "typeParams", buildTypeParamsDeclaration(domainSpec, superDomain.reference(), superDomain.constraints(), imports, cfg)
      ));
    }
    return parens;
  }

  static String buildTypeParamsDeclaration(
      DomainSpecification baseDomainSpec,
      SpaceReference destinationDomain,
      List<ConstraintSpecification> constraints,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    DomainSpecification destinationDomainSpec = findDomain(destinationDomain, cfg);
    List<ContextChannelSpecification> destinationDomainTypeRelatedChannels = getTypeRelatedChannels(destinationDomainSpec, cfg);
    if (destinationDomainTypeRelatedChannels.isEmpty()) {
      return "";
    }

    Map<String, ContextChannelSpecification> baseDomainProjectionAliasToChannelIndex = baseDomainSpec.channels().stream()
        .collect(Collectors.toMap(ContextChannelSpecification::alias, Function.identity()));

    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(constraints);

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("<");
    for (ContextChannelSpecification destinationDomainTypeRelatedChannel : destinationDomainTypeRelatedChannels) {
      ContextChannelSpecification baseChannel = findEquivalentBaseChannel(
          destinationDomainTypeRelatedChannel, equivalenceIndex, baseDomainProjectionAliasToChannelIndex
      );
      if (baseChannel != null) {
        commaAppender.run();
        if (baseChannel.targetAlias() != null) {
          sb.append(baseChannel.targetAlias());
        } else if (baseChannel.targetInstance() != null) {
          if (baseChannel.targetInstance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(baseChannel.targetInstance().asString())));
          } else {
            throw NotImplementedExceptions.withCode("H7Nnygs");
          }
        } else {
          throw NotImplementedExceptions.withCode("8DNy410A");
        }
      } else {
        InstanceSpecification targetInstance = findEquivalentTargetInstance(
            destinationDomainTypeRelatedChannel, equivalenceIndex
        );
        if (targetInstance != null) {
          if (targetInstance.isCustomInstance()) {
            CustomInstanceSpecification customTargetInstance = targetInstance.asCustomInstance();
            String instanceDomainName = customTargetInstance.domain().name();
            BasicDomain instanceBasicDomain = BasicDomains.active().getByDomainName(instanceDomainName);
            if (instanceBasicDomain != null) {
              if (BasicDomainPurposes.Domain.is(instanceBasicDomain.purpose())) {
                String domainName = customTargetInstance.projections().get("name").asString();
                BasicDomain basicDomain = BasicDomains.active().getByDomainName(domainName);
                if (basicDomain != null && BasicDomainPurposes.String.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(String.class));
                } else if (basicDomain != null && BasicDomainPurposes.Integer.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(Integer.class));
                } else if (basicDomain != null && BasicDomainPurposes.Double.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(Double.class));
                } else {
                  sb.append("? extends ");
                  sb.append(imports.addAndGetSimpleName(getDomainClassCanonicalName(domainName)));

                  SpaceReference domainRef = SpaceReferences.build().name(domainName).build();
                  String nestedDeclaration = buildTypeParamsDeclaration(
                      baseDomainSpec, domainRef, customTargetInstance.constraints(), imports, cfg
                  );
                  sb.append(nestedDeclaration);
                }
              } else {
                throw NotImplementedExceptions.withCode("OpXfQG1Q");
              }
            } else {
              throw NotImplementedExceptions.withCode("9p0FcIAP");
            }
          } else {
            throw NotImplementedExceptions.withCode("TuOJF1pE");
          }
        } else {
          throw new MojoExecutionException("Cannot to find equivalent channel of base domain or equivalent target instance");
        }
      }
    }
    sb.append(">");
    return sb.toString();
  }

  static ContextChannelSpecification findEquivalentBaseChannel(
      ContextChannelSpecification destinationDomainChannel,
      Map<TraversePathSpecification, Equivalence> equivalenceIndex,
      Map<String, ContextChannelSpecification> baseDomainProjectionAliasToChannelIndex
  ) throws MojoExecutionException {
    try {
      TraversePathSpecification destinationDomainPath = TraversePathParseFunctions.parse(
          "this thru " + destinationDomainChannel.alias()
      );
      Equivalence equivalence = equivalenceIndex.get(destinationDomainPath);
      for (TraversePathSpecification equivalentPath : equivalence.equivalentPaths()) {
        if ("base".equals(equivalentPath.sourceDomain().name())) {
          if (equivalentPath.transitions().size() == 1) {
            TraverseTransitionSpecification transition = equivalentPath.transitions().get(0);
            if (transition.isThruTransition()) {
              TraverseTransitionThruSpecification thruTransition = transition.asThruTransition();
              ContextChannelSpecification baseChannel = baseDomainProjectionAliasToChannelIndex.get(thruTransition.channel().name());
              if (baseChannel != null) {
                return baseChannel;
              }
            }
          }
        }
      }
      return null;
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Cannot to define equivalent channel of base domain", e);
    }
  }

  static InstanceSpecification findEquivalentTargetInstance(
      ContextChannelSpecification destinationDomainChannel,
      Map<TraversePathSpecification, Equivalence> equivalenceIndex
  ) throws MojoExecutionException {
    try {
      TraversePathSpecification destinationDomainPath = TraversePathParseFunctions.parse(
          "this thru " + destinationDomainChannel.alias()
      );
      Equivalence equivalence = equivalenceIndex.get(destinationDomainPath);
      return equivalence.equivalentInstance();
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Cannot to define equivalent instance", e);
    }
  }

  static List<Map<String, Object>> buildChannelTemplateVariables(
      DomainSpecification baseDomainSpec, MutableImportList imports, Configuration cfg
  ) throws MojoExecutionException {
    var variables = new ArrayList<Map<String, Object>>();
    for (ContextChannelSpecification channelSpec : baseDomainSpec.channels()) {
      try {
        var map = new HashMap<String, Object>();
        map.put("alias", channelSpec.alias());
        map.put("cid", channelSpec.cid());
        map.put("name", channelSpec.name());
        if (ImmobilityTypes.Unmovable.is(channelSpec.targetImmobilityType())) {
          imports.add(Unmovable.class);
          map.put("unmovable", true);
        } else {
          map.put("unmovable", false);
        }
        map.put("typeParams", buildTypeParamDeclarations(channelSpec, imports));
        map.put("target", buildChannelTargetDeclaration(baseDomainSpec, channelSpec, imports, cfg));
        map.put("qualifiers", buildChannelQualifiers(baseDomainSpec, channelSpec, imports));
        map.put("allowedTraverse", buildAllowedTraverse(channelSpec.allowedTraverse(), imports));
        variables.add(map);
      } catch (Exception e) {
        throw new MojoExecutionException("Could not process channel specification '" + channelSpec.alias() + "'", e);
      }
    }
    if (!variables.isEmpty()) {
      imports.add(Channel.class);
    }
    return variables;
  }

  static String buildChannelTargetDeclaration(
      DomainSpecification domainSpec,
      ContextChannelSpecification channelSpec,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException  {
    SpaceReference targetDomainReference = channelSpec.targetDomain();
    if (targetDomainReference != null && targetDomainReference.name() != null) {
      String targetDomainName = targetDomainReference.name();
      String targetDomainClassName = getDefaultDomainClassName(targetDomainName);
      String targetDomainClassSimpleName = imports.addAndGetSimpleName(targetDomainClassName);
      if (BasicDomains.active().isDomainDomain(targetDomainName)) {
        var sb = new StringBuilder();
        sb.append(targetDomainClassSimpleName);
        sb.append("<");
        if (channelSpec.targetAlias() != null) {
          sb.append(channelSpec.targetAlias());
        } else if (channelSpec.targetInstance() != null) {
          if (channelSpec.targetInstance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(channelSpec.targetInstance().asString())));
          } else {
            throw NotImplementedExceptions.withCode("bwPPqkJ9");
          }
        } else {
          sb.append("?");
        }
        sb.append(">");
        return sb.toString();
      }
      return targetDomainClassSimpleName + buildTypeParamsDeclaration(
          domainSpec, targetDomainReference, channelSpec.targetConstraints(), imports, cfg
      );
    } else if (channelSpec.targetDomainBounds() != null) {
      var sb = new StringBuilder();
      sb.append(imports.addAndGetSimpleName(getDomainOfDomainsClassCanonicalName()));
      sb.append("<");
      if (channelSpec.targetAlias() != null) {
        sb.append(channelSpec.targetAlias());
      } else if (channelSpec.targetInstance() != null) {
        if (channelSpec.targetInstance().isString()) {
          sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(channelSpec.targetInstance().asString())));
        } else {
          throw NotImplementedExceptions.withCode("bwPPqkJ9");
        }
      } else {
        throw NotImplementedExceptions.withCode("dAA4NgvI");
      }
      sb.append(">");
      return sb.toString();
    } else if (channelSpec.targetAlias() != null) {
      return channelSpec.targetAlias();
    } else if (!CollectionFunctions.isNullOrEmpty(channelSpec.targetConstraints())) {
      String targetDeclaration = buildChannelTargetDeclarationByConstraints(domainSpec, channelSpec, imports);
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("ymDLHA");
  }

  static String buildChannelTargetDeclarationByConstraints(
      DomainSpecification baseDomainSpec,
      ContextChannelSpecification channelSpec,
      MutableImportList imports
  ) throws MojoExecutionException {
    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(
        channelSpec.targetConstraints()
    );
    TraversePathSpecification pathFromThisToDomain = getPathFromThisToDomain();
    Equivalence equivalence = equivalenceIndex.get(pathFromThisToDomain);
    if (equivalence != null) {
      for (TraversePathSpecification equivalentPath : equivalence.equivalentPaths()) {
        if ("base".equals(equivalentPath.sourceDomain().name())) {
          if (equivalentPath.transitions().size() == 1) {
            TraverseTransitionSpecification transition = equivalentPath.transitions().get(0);
            if (transition.isThruTransition()) {
              TraverseTransitionThruSpecification thruTransition = transition.asThruTransition();
              ContextChannelSpecification channel = baseDomainSpec.channels().stream()
                  .filter(c -> thruTransition.channel().name().equals(c.alias()))
                  .collect(tech.intellispaces.commons.base.stream.Collectors.one());
              if (channel.targetAlias() != null) {
                return channel.targetAlias();
              } else if (channel.targetInstance() != null && channel.targetInstance().isString()) {
                if (BasicDomains.active().isDomainDomain(channel.targetDomain().name())) {
                  return imports.addAndGetSimpleName(getDefaultDomainClassName(channel.targetInstance().asString()));
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  static List<Map<String, Object>> buildChannelQualifiers(
      DomainSpecification baseDomainSpec, ContextChannelSpecification channelSpec, MutableImportList imports
  ) throws MojoExecutionException {
    return CollectionFunctions.mapEach(channelSpec.projections(), qs -> buildChannelValueQualifier(baseDomainSpec, qs, imports));
  }

  static Map<String, Object> buildChannelValueQualifier(
      DomainSpecification baseDomainSpec, ContextChannelSpecification qualifierChannel, MutableImportList imports
  ) throws MojoExecutionException {
    var map = new HashMap<String, Object>();
    map.put("name", qualifierChannel.targetAlias());
    map.put("class", buildChannelValueQualifierTargetDeclaration(baseDomainSpec, qualifierChannel, imports));
    return map;
  }

  static String buildChannelValueQualifierTargetDeclaration(
      DomainSpecification baseDomainSpec,
      ContextChannelSpecification qualifierChannel,
      MutableImportList imports
  ) throws MojoExecutionException {
    if (qualifierChannel.targetDomain() != null) {
      return imports.addAndGetSimpleName(getDefaultDomainClassName(qualifierChannel.targetDomain().name()));
    } else if (!CollectionFunctions.isNullOrEmpty(qualifierChannel.targetConstraints())) {
      String targetDeclaration = buildChannelTargetDeclarationByConstraints(baseDomainSpec, qualifierChannel, imports);
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("ouXtNfZV");
  }

  static boolean isTypeRelatedChannel(ContextChannelSpecification channelSpec) {
    if (channelSpec.targetDomain() != null) {
      if (BasicDomains.active().isDomainDomain(channelSpec.targetDomain().name())) {
        return channelSpec.targetInstance() == null;
      }
    }
    if (channelSpec.targetDomainBounds() != null) {
      return true;
    }
    return false;
  }

  static List<ContextChannelSpecification> getTypeRelatedChannels(DomainSpecification domainSpec, Configuration cfg) {
    return domainSpec.channels().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
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

  static DomainSpecification findDomain(
      SpaceReference domainReference, Configuration cfg
  ) throws MojoExecutionException {
    try {
      return cfg.repository().findDomain(domainReference.name());
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Could not find domain by name '" + domainReference.name() + "'", e);
    }
  }

  static String getDomainClassName(DomainSpecification domainSpec) {
    return NameConventionFunctions.convertIntelliSpacesDomainName(domainSpec.name());
  }

  static String getDefaultDomainClassName(SpaceReference domainReference) {
    return getDefaultDomainClassName(domainReference.name());
  }

  static String getDomainClassName(SpaceReference domainReference) {
    return getDomainClassCanonicalName(domainReference.name());
  }

  static String getDefaultDomainClassName(String domainName) {
    BasicDomain basicDomain = BasicDomains.active().getByDomainName(domainName);
    if (basicDomain != null) {
      return basicDomain.delegateClassName();
    }
    return NameConventionFunctions.convertIntelliSpacesDomainName(domainName);
  }

  static String getDomainClassCanonicalName(String domainName) {
    return NameConventionFunctions.convertIntelliSpacesDomainName(domainName);
  }

  static String getDomainOfDomainsName() {
    return BasicDomains.active().getByDomainType(BasicDomainPurposes.Domain).get(0).domainName();
  }

  static String getDomainOfDomainsClassCanonicalName() {
    return BasicDomains.active().getByDomainType(BasicDomainPurposes.Domain).get(0).delegateClassName();
  }

  static Map<TraversePathSpecification, Equivalence> makeEquivalenceIndex(List<ConstraintSpecification> constraints) {
    var index = new HashMap<TraversePathSpecification, Equivalence>();
    for (ConstraintSpecification constraint : constraints) {
      if (constraint.isEquivalenceConstraint()) {
        EquivalenceConstraintSpecification equivalenceConstraint = constraint.asEquivalenceConstraint();
        for (TraversePathSpecification path : equivalenceConstraint.traversePaths()) {
          Equivalence equivalence = index.computeIfAbsent(path,
              key -> new Equivalence(path, new HashSet<>(), equivalenceConstraint.instance())
          );
          for (TraversePathSpecification path2 : equivalenceConstraint.traversePaths()) {
            if (!path2.equals(path)) {
              equivalence.equivalentPaths().add(path2);
            }
          }
        }
      }
    }
    return index;
  }

  static TraversePathSpecification getPathFromThisToDomain() throws MojoExecutionException {
    if (PATH_FROM_THIS_TO_DOMAIN != null) {
      return PATH_FROM_THIS_TO_DOMAIN;
    }
    try {
      PATH_FROM_THIS_TO_DOMAIN = TraversePathParseFunctions.parse("this to " + getDomainOfDomainsName());
    } catch (TraversePathSpecificationException e) {
      throw new MojoExecutionException("Cannot to build traverse path from THIS to domain");
    }
    return PATH_FROM_THIS_TO_DOMAIN;
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

  record Equivalence(
      TraversePathSpecification path,
      Set<TraversePathSpecification> equivalentPaths,
      InstanceSpecification equivalentInstance
  ) {
  }

  private static TraversePathSpecification PATH_FROM_THIS_TO_DOMAIN;

  private GenerationFunctions() {}
}
