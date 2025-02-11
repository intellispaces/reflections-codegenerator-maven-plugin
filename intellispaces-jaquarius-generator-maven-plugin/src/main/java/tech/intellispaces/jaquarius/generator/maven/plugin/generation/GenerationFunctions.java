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
import tech.intellispaces.core.specification.AllowedTraverseType;
import tech.intellispaces.core.specification.AllowedTraverseTypes;
import tech.intellispaces.core.specification.ChannelSpecification;
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
import tech.intellispaces.jaquarius.channel.MappingChannel;
import tech.intellispaces.jaquarius.channel.MappingOfMovingChannel;
import tech.intellispaces.jaquarius.channel.MovingChannel;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.naming.NameConventionFunctions;
import tech.intellispaces.jaquarius.space.channel.ChannelFunctions;
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
    generateChannels(spec.ontology().channels(), cfg);
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

  static void generateChannels(
      List<ChannelSpecification> channelSpecs, Configuration cfg
  ) throws MojoExecutionException {
    Template template = Templates.get("/channel.template");
    for (ChannelSpecification channelSpec : channelSpecs) {
      try {
        String canonicalName = getChannelClassName(channelSpec);
        Map<String, Object> templateVars = buildChannelTemplateVariables(channelSpec, canonicalName, cfg);
        String source = template.resolve(templateVars);
        write(cfg, canonicalName, source);
      } catch (Exception e) {
        throw new MojoExecutionException("Could not process channel specification " + channelSpec.name(), e);
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

  static Map<String, Object> buildChannelTemplateVariables(
      ChannelSpecification channelSpec, String canonicalName, Configuration cfg
  ) throws MojoExecutionException {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Channel.class);

    var vars = new HashMap<String, Object>();
    vars.put("cid", channelSpec.cid());
    vars.put("methodName", StringFunctions.lowercaseFirstLetter(ClassNameFunctions.getSimpleName(channelSpec.name())));
    vars.put("channelKind", imports.addAndGetSimpleName(ChannelFunctions.getChannelClass(channelSpec.qualifiers().size())));
    vars.put("channelTypes", buildChannelTypes(channelSpec, imports));
    vars.put("typeParams", buildTypeParamDeclarations(channelSpec, imports));
    vars.put("sourceDomain", buildChannelSourceDeclaration(channelSpec, imports));
    vars.put("targetDomain", buildChannelTargetDeclaration(channelSpec, imports));
    vars.put("qualifiers", buildChannelQualifiers(channelSpec, imports));
    vars.put("packageName", ClassNameFunctions.getPackageName(canonicalName));
    vars.put("simpleName", ClassNameFunctions.getSimpleName(canonicalName));
    vars.put("importedClasses", imports.getImports());
    return vars;
  }

  static List<String> buildChannelTypes(ChannelSpecification channelSpec, MutableImportList imports) {
    List<String> channelTypes = new ArrayList<>();
    for (AllowedTraverseType allowedTraverseType : channelSpec.allowedTraverses()) {
      if (AllowedTraverseTypes.Mapping.is(allowedTraverseType)) {
        channelTypes.add(imports.addAndGetSimpleName(MappingChannel.class));
      } else if (AllowedTraverseTypes.Moving.is(allowedTraverseType)) {
        channelTypes.add(imports.addAndGetSimpleName(MovingChannel.class));
      } else if (AllowedTraverseTypes.MappingOfMoving.is(allowedTraverseType)) {
        channelTypes.add(imports.addAndGetSimpleName(MappingOfMovingChannel.class));
      } else {
        throw NotImplementedExceptions.withCode("cfSM0K2N");
      }
    }
    return channelTypes;
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
      ChannelSpecification channelSpec, MutableImportList imports
  ) {
    return channelSpec.qualifiers().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
        .map(c -> buildTypeParamDeclaration(c, imports))
        .toList();
  }

  static String buildTypeParamDeclaration(
      ChannelSpecification channelSpec, MutableImportList imports
  ) {
    if (channelSpec.target().domainBounds() == null) {
      return channelSpec.target().alias();
    }

    var sb = new StringBuilder();
    sb.append(channelSpec.target().alias());
    sb.append(" extends ");
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    for (SpaceReference extendedDomain : channelSpec.target().domainBounds().superDomains()) {
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
    List<ChannelSpecification> destinationDomainTypeRelatedChannels = getTypeRelatedChannels(destinationDomainSpec, cfg);
    if (destinationDomainTypeRelatedChannels.isEmpty()) {
      return "";
    }

    Map<String, ChannelSpecification> baseDomainProjectionAliasToChannelIndex = baseDomainSpec.channels().stream()
        .collect(Collectors.toMap(ChannelSpecification::alias, Function.identity()));

    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(constraints);

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("<");
    for (ChannelSpecification destinationDomainTypeRelatedChannel : destinationDomainTypeRelatedChannels) {
      ChannelSpecification baseChannel = findEquivalentBaseChannel(
          destinationDomainTypeRelatedChannel, equivalenceIndex, baseDomainProjectionAliasToChannelIndex
      );
      if (baseChannel != null) {
        commaAppender.run();
        if (baseChannel.target().alias() != null) {
          sb.append(baseChannel.target().alias());
        } else if (baseChannel.target().instance() != null) {
          if (baseChannel.target().instance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(baseChannel.target().instance().asString())));
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

  static ChannelSpecification findEquivalentBaseChannel(
      ChannelSpecification destinationDomainChannel,
      Map<TraversePathSpecification, Equivalence> equivalenceIndex,
      Map<String, ChannelSpecification> baseDomainProjectionAliasToChannelIndex
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
              ChannelSpecification baseChannel = baseDomainProjectionAliasToChannelIndex.get(thruTransition.channel().name());
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
      ChannelSpecification destinationDomainChannel,
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
    for (ChannelSpecification channelSpec : baseDomainSpec.channels()) {
      try {
        var map = new HashMap<String, Object>();
        map.put("alias", channelSpec.alias());
        map.put("cid", channelSpec.cid());
        map.put("name", channelSpec.name());
        if (ImmobilityTypes.Unmovable.is(channelSpec.target().immobilityType())) {
          imports.add(Unmovable.class);
          map.put("unmovable", true);
        } else {
          map.put("unmovable", false);
        }
        map.put("typeParams", buildTypeParamDeclarations(channelSpec, imports));
        map.put("target", buildChannelTargetDeclaration(baseDomainSpec, channelSpec, imports, cfg));
        map.put("qualifiers", buildChannelQualifiers(baseDomainSpec, channelSpec, imports));
        map.put("allowedTraverse", buildAllowedTraverse(channelSpec.allowedTraverses(), imports));
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
      ChannelSpecification channelSpec,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException  {
    SpaceReference targetDomainReference = channelSpec.target().domain();
    if (targetDomainReference != null && targetDomainReference.name() != null) {
      String targetDomainName = targetDomainReference.name();
      String targetDomainClassName = getDefaultDomainClassName(targetDomainName);
      String targetDomainClassSimpleName = imports.addAndGetSimpleName(targetDomainClassName);
      if (BasicDomains.active().isDomainDomain(targetDomainName)) {
        var sb = new StringBuilder();
        sb.append(targetDomainClassSimpleName);
        sb.append("<");
        if (channelSpec.target().alias() != null) {
          sb.append(channelSpec.target().alias());
        } else if (channelSpec.target().instance() != null) {
          if (channelSpec.target().instance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(channelSpec.target().instance().asString())));
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
          domainSpec, targetDomainReference, channelSpec.target().constraints(), imports, cfg
      );
    } else if (channelSpec.target().domainBounds() != null) {
      var sb = new StringBuilder();
      sb.append(imports.addAndGetSimpleName(getDomainOfDomainsClassCanonicalName()));
      sb.append("<");
      if (channelSpec.target().alias() != null) {
        sb.append(channelSpec.target().alias());
      } else if (channelSpec.target().instance() != null) {
        if (channelSpec.target().instance().isString()) {
          sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(channelSpec.target().instance().asString())));
        } else {
          throw NotImplementedExceptions.withCode("bwPPqkJ9");
        }
      } else {
        throw NotImplementedExceptions.withCode("dAA4NgvI");
      }
      sb.append(">");
      return sb.toString();
    } else if (channelSpec.target().alias() != null) {
      return channelSpec.target().alias();
    } else if (!CollectionFunctions.isNullOrEmpty(channelSpec.target().constraints())) {
      String targetDeclaration = buildChannelDeclarationByConstraints(
          domainSpec.channels(), channelSpec.target().constraints(), imports
      );
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("ymDLHA");
  }

  static String buildChannelDeclarationByConstraints(
      List<ChannelSpecification> baseChannels,
      List<ConstraintSpecification> constraints,
      MutableImportList imports
  ) throws MojoExecutionException {
    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(constraints);
    TraversePathSpecification pathFromThisToDomain = getPathFromThisToDomain();
    Equivalence equivalence = equivalenceIndex.get(pathFromThisToDomain);
    if (equivalence != null) {
      for (TraversePathSpecification equivalentPath : equivalence.equivalentPaths()) {
        if ("base".equals(equivalentPath.sourceDomain().name())) {
          if (equivalentPath.transitions().size() == 1) {
            TraverseTransitionSpecification transition = equivalentPath.transitions().get(0);
            if (transition.isThruTransition()) {
              TraverseTransitionThruSpecification thruTransition = transition.asThruTransition();
              ChannelSpecification channel = baseChannels.stream()
                  .filter(c -> thruTransition.channel().name().equals(c.alias()))
                  .collect(tech.intellispaces.commons.base.stream.Collectors.one());
              if (channel.target().alias() != null) {
                return channel.target().alias();
              } else if (channel.target().instance() != null && channel.target().instance().isString()) {
                if (BasicDomains.active().isDomainDomain(channel.target().domain().name())) {
                  return imports.addAndGetSimpleName(getDefaultDomainClassName(channel.target().instance().asString()));
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
      ChannelSpecification channelSpec, MutableImportList imports
  ) throws MojoExecutionException {
    return CollectionFunctions.mapEach(channelSpec.qualifiers(), qs -> buildChannelValueQualifier(channelSpec, qs, imports));
  }

  static Map<String, Object> buildChannelValueQualifier(
      ChannelSpecification channelSpec, ChannelSpecification qualifierChannel, MutableImportList imports
  ) throws MojoExecutionException {
    var map = new HashMap<String, Object>();
    map.put("alias", qualifierChannel.alias());
    map.put("type", buildChannelValueQualifierTargetDeclaration(channelSpec.qualifiers(), qualifierChannel, imports));
    return map;
  }

  static List<Map<String, Object>> buildChannelQualifiers(
      DomainSpecification baseDomainSpec, ChannelSpecification channelSpec, MutableImportList imports
  ) throws MojoExecutionException {
    return CollectionFunctions.mapEach(channelSpec.qualifiers(), qs -> buildChannelValueQualifier(baseDomainSpec, qs, imports));
  }

  static Map<String, Object> buildChannelValueQualifier(
      DomainSpecification baseDomainSpec, ChannelSpecification qualifierChannel, MutableImportList imports
  ) throws MojoExecutionException {
    var map = new HashMap<String, Object>();
    map.put("name", qualifierChannel.alias());
    map.put("class", buildChannelValueQualifierTargetDeclaration(baseDomainSpec.channels(), qualifierChannel, imports));
    return map;
  }

  static String buildChannelSourceDeclaration(
      ChannelSpecification channelSpec,
      MutableImportList imports
  ) throws MojoExecutionException  {
    SpaceReference sourceDomainReference = channelSpec.source().domain();
    if (sourceDomainReference != null && sourceDomainReference.name() != null) {
      String sourceDomainName = sourceDomainReference.name();
      String sourceDomainClassName = getDefaultDomainClassName(sourceDomainName);
      String sourceDomainClassSimpleName = imports.addAndGetSimpleName(sourceDomainClassName);
      if (BasicDomains.active().isDomainDomain(sourceDomainName)) {
        var sb = new StringBuilder();
        sb.append(sourceDomainClassSimpleName);
        sb.append("<");
        sb.append("?");
        sb.append(">");
        return sb.toString();
      }
      return sourceDomainClassSimpleName;
    }
    throw NotImplementedExceptions.withCode("0y66ociK");
  }

  static String buildChannelTargetDeclaration(
      ChannelSpecification channelSpec,
      MutableImportList imports
  ) throws MojoExecutionException  {
    SpaceReference targetDomainReference = channelSpec.target().domain();
    if (targetDomainReference != null && targetDomainReference.name() != null) {
      String targetDomainName = targetDomainReference.name();
      String targetDomainClassName = getDefaultDomainClassName(targetDomainName);
      String targetDomainClassSimpleName = imports.addAndGetSimpleName(targetDomainClassName);
      if (BasicDomains.active().isDomainDomain(targetDomainName)) {
        var sb = new StringBuilder();
        sb.append(targetDomainClassSimpleName);
        sb.append("<");
        sb.append("?");
        sb.append(">");
        return sb.toString();
      }
      return targetDomainClassSimpleName;
    } else if (!CollectionFunctions.isNullOrEmpty(channelSpec.target().constraints())) {
      String targetDeclaration = buildChannelDeclarationByConstraints(
          channelSpec.qualifiers(), channelSpec.target().constraints(), imports
      );
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("xyulK7XH");
  }

  static String buildChannelValueQualifierTargetDeclaration(
      List<ChannelSpecification> baseChannels,
      ChannelSpecification qualifierChannel,
      MutableImportList imports
  ) throws MojoExecutionException {
    if (qualifierChannel.target().domain() != null) {
      if (BasicDomains.active().isDomainDomain(qualifierChannel.target().domain().name())) {
        var sb = new StringBuilder();
        sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(qualifierChannel.target().domain().name())));
        sb.append("<");
        if (qualifierChannel.target().alias() != null) {
          sb.append("? extends ");
          sb.append(qualifierChannel.target().alias());
        } else if (qualifierChannel.target().instance() != null) {
          if (qualifierChannel.target().instance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(qualifierChannel.target().instance().asString())));
          } else {
            throw NotImplementedExceptions.withCode("bwPPqkJ9");
          }
        } else {
          sb.append("?");
        }
        sb.append(">");
        return sb.toString();
      }
      return imports.addAndGetSimpleName(getDefaultDomainClassName(qualifierChannel.target().domain().name()));
    } else if (!CollectionFunctions.isNullOrEmpty(qualifierChannel.target().constraints())) {
      String targetDeclaration = buildChannelDeclarationByConstraints(
          baseChannels, qualifierChannel.target().constraints(), imports
      );
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("ouXtNfZV");
  }

  static boolean isTypeRelatedChannel(ChannelSpecification channelSpec) {
    if (channelSpec.target().domain() != null) {
      if (BasicDomains.active().isDomainDomain(channelSpec.target().domain().name())) {
        return channelSpec.target().instance() == null;
      }
    }
    if (channelSpec.target().domainBounds() != null) {
      return true;
    }
    return false;
  }

  static List<ChannelSpecification> getTypeRelatedChannels(DomainSpecification domainSpec, Configuration cfg) {
    return domainSpec.channels().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
        .toList();
  }

  static String buildAllowedTraverse(List<AllowedTraverseType> allowedTraverses, MutableImportList imports) {
    if (allowedTraverses == null) {
      return null;
    }
    if (allowedTraverses.size() == 1) {
      return imports.addAndGetSimpleName(TraverseTypes.class) + "." + allowedTraverses.get(0).name();
    }

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("{");
    for (AllowedTraverseType allowedTraverse : allowedTraverses) {
      commaAppender.run();
      sb.append(imports.addAndGetSimpleName(TraverseTypes.class))
          .append(".")
          .append(allowedTraverse.name());
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

  static String getChannelClassName(ChannelSpecification channelSpec) {
    return NameConventionFunctions.convertIntelliSpacesChannelName(channelSpec.name());
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
