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
import tech.intellispaces.core.specification.space.AllowedTraverseType;
import tech.intellispaces.core.specification.space.AllowedTraverseTypes;
import tech.intellispaces.core.specification.space.ChannelSideSpecification;
import tech.intellispaces.core.specification.space.ChannelSpecification;
import tech.intellispaces.core.specification.space.DomainSpecification;
import tech.intellispaces.core.specification.space.ImmobilityTypes;
import tech.intellispaces.core.specification.space.Specification;
import tech.intellispaces.core.specification.space.SpecificationItem;
import tech.intellispaces.core.specification.space.SpecificationItemTypes;
import tech.intellispaces.core.specification.space.SuperDomainSpecification;
import tech.intellispaces.core.specification.space.constraint.ConstraintSpecification;
import tech.intellispaces.core.specification.space.constraint.EquivalenceConstraintSpecification;
import tech.intellispaces.core.specification.space.exception.SpecificationException;
import tech.intellispaces.core.specification.space.exception.TraversePathSpecificationException;
import tech.intellispaces.core.specification.space.instance.CustomInstanceSpecification;
import tech.intellispaces.core.specification.space.instance.InstanceSpecification;
import tech.intellispaces.core.specification.space.reference.SpaceReference;
import tech.intellispaces.core.specification.space.reference.SpaceReferences;
import tech.intellispaces.core.specification.space.traverse.TraversePathParseFunctions;
import tech.intellispaces.core.specification.space.traverse.TraversePathSpecification;
import tech.intellispaces.core.specification.space.traverse.TraverseTransitionSpecification;
import tech.intellispaces.core.specification.space.traverse.TraverseTransitionThruSpecification;
import tech.intellispaces.jaquarius.Jaquarius;
import tech.intellispaces.jaquarius.annotation.Channel;
import tech.intellispaces.jaquarius.annotation.Dataset;
import tech.intellispaces.jaquarius.annotation.Movable;
import tech.intellispaces.jaquarius.annotation.Unmovable;
import tech.intellispaces.jaquarius.generator.maven.plugin.configuration.Configuration;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationContext;
import tech.intellispaces.jaquarius.generator.maven.plugin.specification.SpecificationContexts;
import tech.intellispaces.jaquarius.naming.NameConventionFunctions;
import tech.intellispaces.jaquarius.settings.KeyDomain;
import tech.intellispaces.jaquarius.settings.KeyDomainPurposes;
import tech.intellispaces.jaquarius.space.channel.ChannelFunctions;
import tech.intellispaces.jaquarius.traverse.MappingOfMovingTraverse;
import tech.intellispaces.jaquarius.traverse.MappingTraverse;
import tech.intellispaces.jaquarius.traverse.MovingTraverse;
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
import java.util.Objects;
import java.util.Set;

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
        SpecificationContext curContext = SpecificationContexts.get(
            REFERENCE_BASE, domainSpec,
            REFERENCE_CURRENT, domainSpec
        );
        Map<String, Object> templateVars = buildDomainTemplateVariables(domainSpec, canonicalName, curContext, cfg);
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
        SpecificationContext curContext = SpecificationContexts.get(
            REFERENCE_BASE, channelSpec,
            REFERENCE_CURRENT, channelSpec
        );
        Map<String, Object> templateVars = buildChannelTemplateVariables(channelSpec, canonicalName, curContext, cfg);
        String source = template.resolve(templateVars);
        write(cfg, canonicalName, source);
      } catch (Exception e) {
        throw new MojoExecutionException("Could not process channel specification " + channelSpec.name(), e);
      }
    }
  }

  static Map<String, Object> buildDomainTemplateVariables(
      DomainSpecification domainSpec,
      String canonicalName,
      SpecificationContext context,
      Configuration cfg
  ) throws MojoExecutionException {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Domain.class);

    var vars = new HashMap<String, Object>();
    vars.put("did", domainSpec.did());
    if (isDataset(domainSpec)) {
      vars.put("isDataset", true);
      imports.add(Dataset.class);
    } else {
      vars.put("isDataset", false);
    }
    vars.put("typeParams", buildTypeParamDeclarations(domainSpec, imports));
    vars.put("parents", buildParentsTemplateVariables(domainSpec, context, imports, cfg));
    vars.put("channels", buildDomainChannelTemplateVariables(domainSpec, imports, context, cfg));
    vars.put("packageName", ClassNameFunctions.getPackageName(canonicalName));
    vars.put("simpleName", ClassNameFunctions.getSimpleName(canonicalName));
    vars.put("importedClasses", imports.getImports());
    return vars;
  }

  static Map<String, Object> buildChannelTemplateVariables(
      ChannelSpecification channelSpec,
      String canonicalName,
      SpecificationContext context,
      Configuration cfg
  ) throws MojoExecutionException {
    MutableImportList imports = ImportLists.get(canonicalName);
    imports.add(tech.intellispaces.jaquarius.annotation.Channel.class);

    var vars = new HashMap<String, Object>();
    vars.put("cid", channelSpec.cid());
    vars.put("methodName", StringFunctions.lowercaseFirstLetter(ClassNameFunctions.getSimpleName(channelSpec.name())));
    vars.put("channelKind", imports.addAndGetSimpleName(ChannelFunctions.getChannelClass(channelSpec.qualifiers().size())));
    vars.put("channelTypes", buildChannelTypes(channelSpec, imports));
    vars.put("typeParams", buildTypeParamDeclarations(channelSpec, imports));
    vars.put("sourceDomain", buildChannelSourceTypeDeclaration(channelSpec, context, imports, cfg));
    vars.put("targetDomain", buildChannelTargetTypeDeclaration(channelSpec, context, imports, cfg));
    vars.put("qualifiers", buildChannelQualifiers(channelSpec, context, imports, cfg));
    vars.put("packageName", ClassNameFunctions.getPackageName(canonicalName));
    vars.put("simpleName", ClassNameFunctions.getSimpleName(canonicalName));
    vars.put("importedClasses", imports.getImports());
    return vars;
  }

  static List<String> buildChannelTypes(ChannelSpecification channelSpec, MutableImportList imports) {
    List<String> channelTypes = new ArrayList<>();
    for (AllowedTraverseType allowedTraverseType : channelSpec.allowedTraverses()) {
      if (AllowedTraverseTypes.Mapping.is(allowedTraverseType)) {
        channelTypes.add(imports.addAndGetSimpleName(MappingTraverse.class));
      } else if (AllowedTraverseTypes.Moving.is(allowedTraverseType)) {
        channelTypes.add(imports.addAndGetSimpleName(MovingTraverse.class));
      } else if (AllowedTraverseTypes.MappingOfMoving.is(allowedTraverseType)) {
        channelTypes.add(imports.addAndGetSimpleName(MappingOfMovingTraverse.class));
      } else {
        throw NotImplementedExceptions.withCode("cfSM0K2N");
      }
    }
    return channelTypes;
  }

  static List<String> buildTypeParamDeclarations(
      DomainSpecification domainSpec, MutableImportList imports
  ) {
    if (domainSpec.name() != null && Jaquarius.settings().isDomainOfDomains(domainSpec.name())) {
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
      DomainSpecification domainSpec,
      SpecificationContext context,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    var parens = new ArrayList<Map<String, Object>>();
    for (SuperDomainSpecification superDomain : domainSpec.superDomains()) {
      parens.add(Map.of(
          "name", imports.addAndGetSimpleName(getDomainClassName(superDomain.reference())),
          "typeParams", buildTypeParamsDeclaration(superDomain.reference(), superDomain.constraints(), context, imports, cfg)
      ));
    }
    return parens;
  }

  static String buildTypeParamsDeclaration(
      SpaceReference destinationDomain,
      List<ConstraintSpecification> constraints,
      SpecificationContext context,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    DomainSpecification destinationDomainSpec = findDomain(destinationDomain, cfg);
    List<ChannelSpecification> domainTypeRelatedChannels = getTypeRelatedChannels(destinationDomainSpec, cfg);
    if (domainTypeRelatedChannels.isEmpty()) {
      return "";
    }

    var sb = new StringBuilder();
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    sb.append("<");
    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(constraints);
    for (ChannelSpecification domainTypeRelatedChannel : domainTypeRelatedChannels) {
      ChannelSpecification contextChannel = findEquivalentBaseChannel(domainTypeRelatedChannel, equivalenceIndex, context);
      commaAppender.run();
      if (contextChannel != null) {
        if (contextChannel.target().alias() != null) {
          sb.append(contextChannel.target().alias());
        } else if (contextChannel.target().instance() != null) {
          if (contextChannel.target().instance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(contextChannel.target().instance().asString())));
          } else {
            throw NotImplementedExceptions.withCode("H7Nnygs");
          }
        } else {
          throw NotImplementedExceptions.withCode("8DNy410A");
        }
      } else {
        InstanceSpecification targetInstance = findEquivalentTargetInstance(
            domainTypeRelatedChannel, equivalenceIndex
        );
        if (targetInstance != null) {
          if (targetInstance.isCustomInstance()) {
            CustomInstanceSpecification customTargetInstance = targetInstance.asCustomInstance();
            String instanceDomainName = customTargetInstance.domain().name();
            KeyDomain instanceBasicDomain = Jaquarius.settings().getKeyDomainByName(instanceDomainName);
            if (instanceBasicDomain != null) {
              if (KeyDomainPurposes.Domain.is(instanceBasicDomain.purpose())) {
                String domainName = customTargetInstance.projections().get("name").asString();
                KeyDomain basicDomain = Jaquarius.settings().getKeyDomainByName(domainName);
                if (basicDomain != null && KeyDomainPurposes.Point.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(Object.class));
                } else if (basicDomain != null && KeyDomainPurposes.String.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(String.class));
                } else if (basicDomain != null && KeyDomainPurposes.Byte.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(Byte.class));
                } else if (basicDomain != null && KeyDomainPurposes.Integer.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(Integer.class));
                } else if (basicDomain != null && KeyDomainPurposes.Double.is(basicDomain.purpose())) {
                  sb.append(imports.addAndGetSimpleName(Double.class));
                } else {
                  sb.append("? extends ");
                  sb.append(imports.addAndGetSimpleName(getDomainClassCanonicalName(domainName)));

                  SpaceReference domainRef = SpaceReferences.build().name(domainName).build();
                  String nestedDeclaration = buildTypeParamsDeclaration(
                      domainRef, customTargetInstance.constraints(), context, imports, cfg
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
      ChannelSpecification domainTypeRelatedChannel,
      Map<TraversePathSpecification, Equivalence> equivalenceIndex,
      SpecificationContext context
  ) throws MojoExecutionException {
    try {
      TraversePathSpecification domainPath = TraversePathParseFunctions.parse(
          REFERENCE_CURRENT + " thru " + domainTypeRelatedChannel.alias()
      );
      Equivalence equivalence = equivalenceIndex.get(domainPath);
      for (TraversePathSpecification equivalentPath : equivalence.equivalentPaths()) {
        if (equivalentPath.transitions().size() == 1) {
          TraverseTransitionSpecification transition = equivalentPath.transitions().get(0);
          if (transition.isThruTransition()) {
            TraverseTransitionThruSpecification thruTransition = transition.asThruTransition();

            String source = equivalentPath.sourceDomain().name();
            List<ChannelSpecification> contextChannels = getContextChannels(context, source);
            for (ChannelSpecification contextChannel : contextChannels) {
              if (Objects.equals(contextChannel.alias(),  thruTransition.channel().name())) {
                return contextChannel;
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
          REFERENCE_CURRENT + " thru " + destinationDomainChannel.alias()
      );
      Equivalence equivalence = equivalenceIndex.get(destinationDomainPath);
      return equivalence.equivalentInstance();
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Cannot to define equivalent instance", e);
    }
  }

  static List<Map<String, Object>> buildDomainChannelTemplateVariables(
      DomainSpecification baseDomainSpec,
      MutableImportList imports,
      SpecificationContext parentContext,
      Configuration cfg
  ) throws MojoExecutionException {
    var variables = new ArrayList<Map<String, Object>>();
    for (ChannelSpecification channelSpec : baseDomainSpec.channels()) {
      try {
        SpecificationContext context = SpecificationContexts.get(parentContext,
            REFERENCE_CURRENT, channelSpec,
            "$" + channelSpec.alias(), channelSpec
        );

        var map = new HashMap<String, Object>();
        map.put("alias", channelSpec.alias());
        map.put("cid", channelSpec.cid());
        map.put("name", channelSpec.name());
        if (ImmobilityTypes.Unmovable.is(channelSpec.target().immobilityType())) {
          imports.add(Unmovable.class);
          map.put("movable", false);
          map.put("unmovable", true);
        } else if (ImmobilityTypes.Movable.is(channelSpec.target().immobilityType())) {
          imports.add(Movable.class);
          map.put("movable", true);
          map.put("unmovable", false);
        } else {
          map.put("movable", false);
          map.put("unmovable", false);
        }
        map.put("typeParams", buildTypeParamDeclarations(channelSpec, imports));
        map.put("target", buildChannelTargetTypeDeclaration(channelSpec, context, imports, cfg));
        map.put("qualifiers", buildChannelQualifiers(channelSpec, context, imports, cfg));
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

  static String buildChannelSourceTypeDeclaration(
      ChannelSpecification channelSpec,
      SpecificationContext parentContext,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    return buildChannelSideTypeDeclaration(channelSpec.source(), parentContext, imports, cfg);
  }

  static String buildChannelTargetTypeDeclaration(
      ChannelSpecification channelSpec,
      SpecificationContext parentContext,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    return buildChannelSideTypeDeclaration(channelSpec.target(), parentContext, imports, cfg);
  }

  static String buildChannelSideTypeDeclaration(
      ChannelSideSpecification channelSideSpec,
      SpecificationContext context,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException  {
    SpaceReference domainReference = channelSideSpec.domain();
    if (domainReference != null && domainReference.name() != null) {
      String domainName = domainReference.name();
      String domainClassName = getDefaultDomainClassName(domainName);
      String domainClassSimpleName = imports.addAndGetSimpleName(domainClassName);
      if (Jaquarius.settings().isDomainOfDomains(domainName)) {
        var sb = new StringBuilder();
        sb.append(domainClassSimpleName);
        sb.append("<");
        if (channelSideSpec.alias() != null) {
          sb.append(channelSideSpec.alias());
        } else if (channelSideSpec.instance() != null) {
          if (channelSideSpec.instance().isString()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(channelSideSpec.instance().asString())));
          } else {
            throw NotImplementedExceptions.withCode("bwPPqkJ9");
          }
        } else {
          sb.append("?");
        }
        sb.append(">");
        return sb.toString();
      }
      return domainClassSimpleName + buildTypeParamsDeclaration(
          domainReference, channelSideSpec.constraints(), context, imports, cfg
      );
    } else if (channelSideSpec.domainBounds() != null) {
      var sb = new StringBuilder();
      sb.append(imports.addAndGetSimpleName(getDomainOfDomainsClassCanonicalName()));
      sb.append("<");
      if (channelSideSpec.alias() != null) {
        sb.append(channelSideSpec.alias());
      } else if (channelSideSpec.instance() != null) {
        if (channelSideSpec.instance().isString()) {
          sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(channelSideSpec.instance().asString())));
        } else {
          throw NotImplementedExceptions.withCode("bwPPqkJ9");
        }
      } else {
        throw NotImplementedExceptions.withCode("dAA4NgvI");
      }
      sb.append(">");
      return sb.toString();
    } else if (channelSideSpec.alias() != null) {
      return channelSideSpec.alias();
    } else if (!CollectionFunctions.isNullOrEmpty(channelSideSpec.constraints())) {
      String targetDeclaration = buildChannelDeclarationByConstraints(context, channelSideSpec.constraints(), imports);
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("ymDLHA");
  }

  static String buildChannelDeclarationByConstraints(
      SpecificationContext context,
      List<ConstraintSpecification> constraints,
      MutableImportList imports
  ) throws MojoExecutionException {
    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(constraints);
    TraversePathSpecification pathFromThisToDomain = getPathFromThisToDomain();
    Equivalence equivalence = equivalenceIndex.get(pathFromThisToDomain);
    if (equivalence != null) {
      for (TraversePathSpecification equivalentPath : equivalence.equivalentPaths()) {
        String source = equivalentPath.sourceDomain().name();
        List<ChannelSpecification> channels = getContextChannels(context, source);
        if (!channels.isEmpty()) {
          if (equivalentPath.transitions().size() == 1) {
            TraverseTransitionSpecification transition = equivalentPath.transitions().get(0);
            if (transition.isThruTransition()) {
              TraverseTransitionThruSpecification thruTransition = transition.asThruTransition();
              ChannelSpecification channel = channels.stream()
                  .filter(c -> thruTransition.channel().name().equals(c.alias()))
                  .collect(tech.intellispaces.commons.base.stream.Collectors.one());
              if (channel.target().alias() != null) {
                return channel.target().alias();
              } else if (channel.target().instance() != null && channel.target().instance().isString()) {
                if (Jaquarius.settings().isDomainOfDomains(channel.target().domain().name())) {
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
      ChannelSpecification channelSpec,
      SpecificationContext context,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    return CollectionFunctions.mapEach(channelSpec.qualifiers(),
        qualifierChannel -> buildChannelQualifier(qualifierChannel, context, imports, cfg));
  }

  static Map<String, Object> buildChannelQualifier(
      ChannelSpecification qualifierChannel,
      SpecificationContext context,
      MutableImportList imports,
      Configuration cfg
  ) throws MojoExecutionException {
    var map = new HashMap<String, Object>();
    map.put("alias", qualifierChannel.alias());
    map.put("type", buildChannelSideTypeDeclaration(qualifierChannel.target(), context, imports, cfg));
    return map;
  }

  static boolean isDataset(DomainSpecification domainSpec) {
    for (SuperDomainSpecification superDomain : domainSpec.superDomains()) {
      KeyDomain basicDomain = Jaquarius.settings().getKeyDomainByName(superDomain.reference().name());
      if (basicDomain != null && KeyDomainPurposes.Dataset.is(basicDomain.purpose())) {
        return true;
      }
    }
    return false;
  }

  static boolean isTypeRelatedChannel(ChannelSpecification channelSpec) {
    if (channelSpec.target().domain() != null) {
      if (Jaquarius.settings().isDomainOfDomains(channelSpec.target().domain().name())) {
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

  static List<ChannelSpecification> getCurrentContextChannels(SpecificationContext context) {
    return getContextChannels(context, REFERENCE_CURRENT);
  }

  static List<ChannelSpecification> getContextChannels(SpecificationContext context, String reference) {
    SpecificationItem specificationItem = context.get(reference);
    if (specificationItem == null) {
      return List.of();
    } else if (SpecificationItemTypes.Domain.is(specificationItem.type())) {
      return specificationItem.asDomainSpecification().channels();
    } else if (SpecificationItemTypes.Channel.is(specificationItem.type())) {
      return specificationItem.asChannelSpecification().qualifiers();
    } else {
      throw NotImplementedExceptions.withCode("ZS9KOwfp");
    }
  }

  static String getDomainClassName(DomainSpecification domainSpec) {
    return NameConventionFunctions.convertToDomainClassName(domainSpec.name());
  }

  static String getChannelClassName(ChannelSpecification channelSpec) {
    return NameConventionFunctions.convertToChannelClassName(channelSpec.name());
  }

  static String getDefaultDomainClassName(SpaceReference domainReference) {
    return getDefaultDomainClassName(domainReference.name());
  }

  static String getDomainClassName(SpaceReference domainReference) {
    return getDomainClassCanonicalName(domainReference.name());
  }

  static String getDefaultDomainClassName(String domainName) {
    KeyDomain basicDomain = Jaquarius.settings().getKeyDomainByName(domainName);
    if (basicDomain != null && basicDomain.delegateClassName() != null) {
      return basicDomain.delegateClassName();
    }
    return NameConventionFunctions.convertToDomainClassName(domainName);
  }

  static String getDomainClassCanonicalName(String domainName) {
    return NameConventionFunctions.convertToDomainClassName(domainName);
  }

  static String getDomainOfDomainsName() {
    return Jaquarius.settings().getKeyDomainByPurpose(KeyDomainPurposes.Domain).domainName();
  }

  static String getDomainOfDomainsClassCanonicalName() {
    return Jaquarius.settings().getKeyDomainByPurpose(KeyDomainPurposes.Domain).delegateClassName();
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
      PATH_FROM_THIS_TO_DOMAIN = TraversePathParseFunctions.parse(REFERENCE_CURRENT + " to " + getDomainOfDomainsName());
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

  private static final String REFERENCE_CURRENT = "$this";
  private static final String REFERENCE_BASE = "$base";

  private GenerationFunctions() {}
}
