package tech.intellispaces.reflections.codegenerator.mavenplugin.generation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;

import tech.intellispaces.actions.runnable.RunnableAction;
import tech.intellispaces.actions.text.StringActions;
import tech.intellispaces.commons.collection.ArraysFunctions;
import tech.intellispaces.commons.collection.CollectionFunctions;
import tech.intellispaces.commons.exception.NotImplementedExceptions;
import tech.intellispaces.commons.text.StringFunctions;
import tech.intellispaces.commons.type.ClassFunctions;
import tech.intellispaces.commons.type.ClassNameFunctions;
import tech.intellispaces.core.id.IdentifierFunctions;
import tech.intellispaces.javareflection.dependencies.DependencySets;
import tech.intellispaces.javareflection.dependencies.MutableDependencySet;
import tech.intellispaces.reflections.codegenerator.mavenplugin.configuration.Configuration;
import tech.intellispaces.reflections.codegenerator.mavenplugin.specification.SpecificationContext;
import tech.intellispaces.reflections.codegenerator.mavenplugin.specification.SpecificationContexts;
import tech.intellispaces.reflections.framework.annotation.Channel;
import tech.intellispaces.reflections.framework.annotation.Dataset;
import tech.intellispaces.reflections.framework.annotation.Movable;
import tech.intellispaces.reflections.framework.id.RepetableUuidIdentifierGenerator;
import tech.intellispaces.reflections.framework.naming.NameConventionFunctions;
import tech.intellispaces.reflections.framework.node.ReflectionsNodeFunctions;
import tech.intellispaces.reflections.framework.settings.DomainAssignments;
import tech.intellispaces.reflections.framework.settings.DomainReference;
import tech.intellispaces.reflections.framework.space.channel.ChannelFunctions;
import tech.intellispaces.reflections.framework.traverse.MappingOfMovingTraverse;
import tech.intellispaces.reflections.framework.traverse.MappingTraverse;
import tech.intellispaces.reflections.framework.traverse.MovingTraverse;
import tech.intellispaces.reflections.framework.traverse.TraverseTypes;
import tech.intellispaces.specification.space.AllowedTraverseType;
import tech.intellispaces.specification.space.AllowedTraverseTypes;
import tech.intellispaces.specification.space.ChannelSideSpecification;
import tech.intellispaces.specification.space.ChannelSpecification;
import tech.intellispaces.specification.space.DomainSpecification;
import tech.intellispaces.specification.space.FileSpecification;
import tech.intellispaces.specification.space.ImmobilityTypes;
import tech.intellispaces.specification.space.SpecificationItem;
import tech.intellispaces.specification.space.SpecificationItemTypes;
import tech.intellispaces.specification.space.SuperDomainSpecification;
import tech.intellispaces.specification.space.constraint.ConstraintSpecification;
import tech.intellispaces.specification.space.constraint.EquivalenceConstraintSpecification;
import tech.intellispaces.specification.space.exception.SpecificationException;
import tech.intellispaces.specification.space.exception.TraversePathSpecificationException;
import tech.intellispaces.specification.space.instance.CustomInstanceSpecification;
import tech.intellispaces.specification.space.instance.InstanceSpecification;
import tech.intellispaces.specification.space.traverse.TraversePathParseFunctions;
import tech.intellispaces.specification.space.traverse.TraversePathSpecification;
import tech.intellispaces.specification.space.traverse.TraverseTransitionSpecification;
import tech.intellispaces.specification.space.traverse.TraverseTransitionThruSpecification;
import tech.intellispaces.templateengine.template.Template;

public class GenerationFunctions {

  public static void generateArtifacts(FileSpecification spec, Configuration cfg) throws MojoExecutionException {
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
        String canonicalName = getDomainClassName(domainSpec, cfg);
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
        String canonicalName = getChannelClassName(channelSpec, cfg);
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
    MutableDependencySet imports = DependencySets.get(canonicalName);

    var vars = new HashMap<String, Object>();
    vars.put("rid", domainSpec.rid().toString());
    vars.put("name", domainSpec.name());
    if (isDatasetDomain(domainSpec, cfg)) {
      vars.put("isDataset", true);
      imports.add(Dataset.class);
    } else {
      vars.put("isDataset", false);
    }
    vars.put("typeParams", buildTypeParamDeclarations(domainSpec, imports, cfg));
    vars.put("parents", buildParentsTemplateVariables(domainSpec, context, imports, cfg));
    vars.put("channels", buildDomainChannelTemplateVariables(domainSpec, imports, context, cfg));
    vars.put("inheritedChannels", buildInheritedChannelTemplateVariables(domainSpec, imports, context, cfg));
    vars.put("packageName", ClassNameFunctions.getPackageName(canonicalName));
    vars.put("simpleName", ClassNameFunctions.getSimpleName(canonicalName));
    vars.put("domainAnnotation", imports.addAndGetSimpleName(tech.intellispaces.reflections.framework.annotation.Domain.class));
    vars.put("channelAnnotation", imports.addAndGetSimpleName(tech.intellispaces.reflections.framework.annotation.Channel.class));
    vars.put("reflectionAnnotation", imports.addAndGetSimpleName(tech.intellispaces.reflections.framework.annotation.Reflection.class));
    vars.put("importedClasses", imports.getImports());
    return vars;
  }

  static Map<String, Object> buildChannelTemplateVariables(
      ChannelSpecification channelSpec,
      String canonicalName,
      SpecificationContext context,
      Configuration cfg
  ) throws MojoExecutionException {
    MutableDependencySet imports = DependencySets.get(canonicalName);

    var vars = new HashMap<String, Object>();
    vars.put("rid", channelSpec.rid().toString());
    vars.put("name", channelSpec.name());
    vars.put("methodName", StringFunctions.lowercaseFirstLetter(ClassNameFunctions.getSimpleName(channelSpec.name())));
    vars.put("channelKind", imports.addAndGetSimpleName(ChannelFunctions.getChannelClass(channelSpec.qualifiers().size())));
    vars.put("channelTypes", buildChannelTypes(channelSpec, imports));
    vars.put("typeParams", buildTypeParamDeclarations(channelSpec, imports, cfg));
    vars.put("sourceDomain", buildChannelSourceTypeDeclaration(channelSpec, context, imports, cfg));
    vars.put("targetDomain", buildChannelTargetTypeDeclaration(channelSpec, channelSpec.source().domainAlias(), context, imports, cfg));
    vars.put("qualifiers", buildChannelQualifiers(channelSpec, context, imports, cfg));
    vars.put("packageName", ClassNameFunctions.getPackageName(canonicalName));
    vars.put("simpleName", ClassNameFunctions.getSimpleName(canonicalName));
    vars.put("channelAnnotation", imports.addAndGetSimpleName(tech.intellispaces.reflections.framework.annotation.Channel.class));
    vars.put("importedClasses", imports.getImports());
    return vars;
  }

  static List<String> buildChannelTypes(ChannelSpecification channelSpec, MutableDependencySet imports) {
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
      DomainSpecification domainSpec, MutableDependencySet imports, Configuration cfg
  ) {
    if (domainSpec.name() != null && ReflectionsNodeFunctions.ontologyReference().isDomainOfDomains(domainSpec.name())) {
      return List.of("D");
    }
    return domainSpec.channels().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
        .map(c -> buildTypeParamDeclaration(c, imports, cfg))
        .toList();
  }

  static List<String> buildTypeParamDeclarations(
      ChannelSpecification channelSpec, MutableDependencySet imports, Configuration cfg
  ) {
    return channelSpec.qualifiers().stream()
        .filter(GenerationFunctions::isTypeRelatedChannel)
        .map(c -> buildTypeParamDeclaration(c, imports, cfg))
        .toList();
  }

  static String buildTypeParamDeclaration(
      ChannelSpecification channelSpec, MutableDependencySet imports, Configuration cfg
  ) {
    if (channelSpec.target().domainBounds() == null) {
      return channelSpec.target().sideAlias();
    }

    var sb = new StringBuilder();
    sb.append(channelSpec.target().sideAlias());
    sb.append(" extends ");
    RunnableAction commaAppender = StringActions.skipFirstTimeCommaAppender(sb);
    for (String extendedDomain : channelSpec.target().domainBounds().superDomainAliases()) {
      commaAppender.run();
      sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(extendedDomain, false, cfg)));
    }
    return sb.toString();
  }

  static List<String> buildParentsTemplateVariables(
      DomainSpecification domainSpec,
      SpecificationContext context,
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    var parents = new ArrayList<String>();
    for (SuperDomainSpecification superDomain : domainSpec.superDomains()) {
      parents.add(imports.addAndGetSimpleName(getDomainClassName(superDomain.alias(), cfg)) +
          buildTypeParamsDeclaration(superDomain.alias(), superDomain.constraints(), context, imports, cfg)
      );
    }
    return parents;
  }

  static String buildTypeParamsDeclaration(
      String destinationDomainAlias,
      List<ConstraintSpecification> constraints,
      SpecificationContext context,
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    DomainReference standardDomain = ReflectionsNodeFunctions.ontologyReference().getDomainByName(destinationDomainAlias);
    if (standardDomain != null
        && !DomainAssignments.Domain.is(standardDomain.assignment())
          && !DomainAssignments.Dataset.is(standardDomain.assignment())
    ) {
      return "";
    }

    DomainSpecification destinationDomainSpec = findDomain(destinationDomainAlias, cfg);
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
        if (contextChannel.target().sideAlias() != null) {
          sb.append(contextChannel.target().sideAlias());
        } else if (contextChannel.target().instance() != null) {
          if (contextChannel.target().instance().isStringInstance()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(contextChannel.target().instance().asString(), false, cfg)));
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
            String instanceDomainName = customTargetInstance.domainAlias();
            DomainReference instanceDomain = ReflectionsNodeFunctions.ontologyReference().getDomainByName(instanceDomainName);
            if (instanceDomain != null) {
              if (DomainAssignments.Domain.is(instanceDomain.assignment())) {
                String domainName = customTargetInstance.projections().get("name").asString();
                DomainReference domain = ReflectionsNodeFunctions.ontologyReference().getDomainByName(domainName);
                if (domain != null && DomainAssignments.Notion.is(domain.assignment())) {
                  sb.append(imports.addAndGetSimpleName(Object.class));
                } else if (domain != null && DomainAssignments.String.is(domain.assignment())) {
                  sb.append(imports.addAndGetSimpleName(String.class));
                } else if (domain != null && DomainAssignments.Byte.is(domain.assignment())) {
                  sb.append(imports.addAndGetSimpleName(Byte.class));
                } else if (domain != null && DomainAssignments.Integer.is(domain.assignment())) {
                  sb.append(imports.addAndGetSimpleName(Integer.class));
                } else if (domain != null && DomainAssignments.Double.is(domain.assignment())) {
                  sb.append(imports.addAndGetSimpleName(Double.class));
                } else {
                  sb.append("? extends ");
                  sb.append(imports.addAndGetSimpleName(getDomainClassCanonicalName(domainName, cfg)));

                  String nestedDeclaration = buildTypeParamsDeclaration(
                      domainName, customTargetInstance.constraints(), context, imports, cfg
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

            String source = equivalentPath.sourceDomainAlias();
            List<ChannelSpecification> contextChannels = getContextChannels(context, source);
            for (ChannelSpecification contextChannel : contextChannels) {
              if (Objects.equals(contextChannel.alias(), thruTransition.channelAlias())) {
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
      DomainSpecification domainSpec,
      MutableDependencySet imports,
      SpecificationContext parentContext,
      Configuration cfg
  ) throws MojoExecutionException {
    var variables = new ArrayList<Map<String, Object>>();
    for (ChannelSpecification channelSpec : domainSpec.channels()) {
      try {
        SpecificationContext context = SpecificationContexts.get(parentContext,
            REFERENCE_CURRENT, channelSpec,
            "$" + channelSpec.alias(), channelSpec
        );

        var map = new HashMap<String, Object>();
        map.put("alias", channelSpec.alias());
        map.put("rid", channelSpec.rid().toString());
        map.put("name", channelSpec.name());
        if (ImmobilityTypes.General.is(channelSpec.target().immobilityType())) {
          map.put("movable", false);
          map.put("unmovable", false);
        } else if (ImmobilityTypes.Movable.is(channelSpec.target().immobilityType())) {
          imports.add(Movable.class);
          map.put("movable", true);
          map.put("unmovable", false);
        } else {
          map.put("movable", false);
          map.put("unmovable", true);
        }
        map.put("typeParams", buildTypeParamDeclarations(channelSpec, imports, cfg));
        map.put("target", buildChannelTargetTypeDeclaration(channelSpec, domainSpec.name(), context, imports, cfg));
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

  static List<Map<String, Object>> buildInheritedChannelTemplateVariables(
      DomainSpecification domainSpec,
      MutableDependencySet imports,
      SpecificationContext parentContext,
      Configuration cfg
  ) throws MojoExecutionException {
    var variables = new ArrayList<Map<String, Object>>();
    List<ChannelSpecification> inheritedChannels = findInheritedChannels(domainSpec, cfg);
    for (ChannelSpecification channelSpec : inheritedChannels) {
      if (channelSpec.allowedTraverses().contains(AllowedTraverseTypes.Moving)) {
        try {
          SpecificationContext context = SpecificationContexts.get(parentContext,
              REFERENCE_CURRENT, channelSpec,
              "$" + channelSpec.alias(), channelSpec
          );

          byte[] seed = ArraysFunctions.join(domainSpec.rid().raw(), channelSpec.rid().raw());
          var identifierGenerator = new RepetableUuidIdentifierGenerator(seed);
          String rid = IdentifierFunctions.convertToHexString(identifierGenerator.next());

          var map = new HashMap<String, Object>();
          map.put("alias", channelSpec.alias());
          map.put("rid", rid);
          map.put("typeParams", buildTypeParamDeclarations(channelSpec, imports, cfg));
          map.put("target", buildInheritedTargetTypeDeclaration(domainSpec, imports, cfg));
          map.put("qualifiers", buildChannelQualifiers(channelSpec, context, imports, cfg));
          variables.add(map);

          imports.add(TraverseTypes.class);
        } catch (Exception e) {
          throw new MojoExecutionException("Could not process channel specification '" + channelSpec.alias() + "'", e);
        }
      }
    }
    return variables;
  }

  static List<ChannelSpecification> findInheritedChannels(
      DomainSpecification domainSpec, Configuration cfg
  ) throws MojoExecutionException {
    try {
      var inheritedChannels = new ArrayList<ChannelSpecification>();
      findInheritedChannels(domainSpec, inheritedChannels, cfg);
      return inheritedChannels;
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Could not collect inherited channels for domain '" + domainSpec.name() + "'", e);
    }
  }

  static void findInheritedChannels(
      DomainSpecification domainSpec, List<ChannelSpecification> inheritedChannels, Configuration cfg
  ) throws SpecificationException {
    for (SuperDomainSpecification superDomainSpec : domainSpec.superDomains()) {
      DomainSpecification superDomain = cfg.repository().findDomain(superDomainSpec.alias());
      inheritedChannels.addAll(superDomain.channels());
      findInheritedChannels(superDomain, inheritedChannels, cfg);
    }
  }

  static String buildChannelSourceTypeDeclaration(
      ChannelSpecification channelSpec,
      SpecificationContext parentContext,
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    return buildChannelSideTypeDeclaration(channelSpec.source(), parentContext, imports, cfg, true);
  }

  static String buildChannelTargetTypeDeclaration(
      ChannelSpecification channelSpec,
      String domainAlias,
      SpecificationContext parentContext,
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    boolean enablePrimitives = !enablePrimitivesForChannelTarget(channelSpec, domainAlias, cfg);
    return buildChannelSideTypeDeclaration(channelSpec.target(), parentContext, imports, cfg, enablePrimitives);
  }

  static boolean enablePrimitivesForChannelTarget(
      ChannelSpecification channelSpec, String domainAlias, Configuration cfg
  ) throws MojoExecutionException {
    if (channelSpec.target().domainAlias() == null) {
      return false;
    }
    String domainClassName = getDefaultDomainClassName(channelSpec.target().domainAlias(), true, cfg);
    if (!ClassFunctions.isPrimitiveClass(domainClassName)) {
      return false;
    }

    DomainSpecification domain = findDomain(domainAlias, cfg);
    for (SuperDomainSpecification superDomainSpec : domain.superDomains()) {
      DomainSpecification superDomain = findDomain(superDomainSpec.alias(), cfg);
      for (ChannelSpecification superChannelSpec : superDomain.channels()) {
        if (Objects.equals(superChannelSpec.alias(), channelSpec.alias()) && superChannelSpec.qualifiers().size() == channelSpec.qualifiers().size()) {
          if (channelSpec.qualifiers().isEmpty()) {
            return (superChannelSpec.target().domainAlias() == null);
          } else {
            boolean sameQualifiers = true;
            Iterator<ChannelSpecification> channelQualifierIterator = channelSpec.qualifiers().iterator();
            Iterator<ChannelSpecification> superChannelQualifierIterator = superChannelSpec.qualifiers().iterator();
            while (channelQualifierIterator.hasNext()) {
              ChannelSpecification channelQualifier = channelQualifierIterator.next();
              ChannelSpecification superChannelQualifier = superChannelQualifierIterator.next();
              if (channelQualifier.target().domainAlias() != null && channelQualifier.target().domainAlias() != null &&
                  superChannelQualifier.target().domainAlias() != null && superChannelQualifier.target().domainAlias() !=null
              ) {
                if (!Objects.equals(channelQualifier.target().domainAlias(), superChannelQualifier.target().domainAlias())) {
                  sameQualifiers = false;
                  break;
                }
              } else {
                throw NotImplementedExceptions.withCode("a0RIoGgNGvM");
              };
            }
            if (sameQualifiers) {
              return (superChannelSpec.target().domainAlias() == null);
            }
          }
        }
      }
      if (enablePrimitivesForChannelTarget(channelSpec, superDomainSpec.alias(), cfg)) {
        return true;
      }
    }
    return false;
  }

  static String buildChannelSideTypeDeclaration(
      ChannelSideSpecification channelSideSpec,
      SpecificationContext context,
      MutableDependencySet imports,
      Configuration cfg,
      boolean enablePrimitives
  ) throws MojoExecutionException  {
    String domainAlias = channelSideSpec.domainAlias();
    if (domainAlias != null) {
      String domainClassName = getDefaultDomainClassName(domainAlias, enablePrimitives, cfg);
      String domainClassSimpleName = imports.addAndGetSimpleName(domainClassName);
      if (ReflectionsNodeFunctions.ontologyReference().isDomainOfDomains(domainAlias)) {
        var sb = new StringBuilder();
        sb.append(domainClassSimpleName);
        sb.append("<");
        if (channelSideSpec.sideAlias() != null) {
          sb.append(channelSideSpec.sideAlias());
        } else if (channelSideSpec.isTargetSide()) {
          InstanceSpecification instance = channelSideSpec.asTargetSide().instance();
          if (instance != null) {
            if (instance.isStringInstance()) {
              sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(instance.asString(), false, cfg)));
            } else {
              throw NotImplementedExceptions.withCode("bwPPqkJ9");
            }
          }
        } else {
          sb.append("?");
        }
        sb.append(">");
        return sb.toString();
      }
      return domainClassSimpleName + buildTypeParamsDeclaration(
          domainAlias, channelSideSpec.constraints(), context, imports, cfg
      );
    } else if (channelSideSpec.domainBounds() != null) {
      var sb = new StringBuilder();
      sb.append(imports.addAndGetSimpleName(getDomainOfDomainsClassCanonicalName()));
      sb.append("<");
      if (channelSideSpec.sideAlias() != null) {
        sb.append(channelSideSpec.sideAlias());
      } else if (channelSideSpec.isTargetSide()) {
        InstanceSpecification instance = channelSideSpec.asTargetSide().instance();
        if (instance != null) {
          if (instance.isStringInstance()) {
            sb.append(imports.addAndGetSimpleName(getDefaultDomainClassName(instance.asString(), false, cfg)));
          } else {
            throw NotImplementedExceptions.withCode("bwPPqkJ9");
          }
        }
      } else {
        throw NotImplementedExceptions.withCode("dAA4NgvI");
      }
      sb.append(">");
      return sb.toString();
    } else if (channelSideSpec.sideAlias() != null) {
      return channelSideSpec.sideAlias();
    } else if (!CollectionFunctions.isNullOrEmpty(channelSideSpec.constraints())) {
      String targetDeclaration = buildChannelDeclarationByConstraints(context, channelSideSpec.constraints(), imports, cfg);
      if (targetDeclaration != null) {
        return targetDeclaration;
      }
    }
    throw NotImplementedExceptions.withCode("ymDLHA");
  }

  static String buildInheritedTargetTypeDeclaration(
      DomainSpecification domainSpec, MutableDependencySet imports, Configuration cfg
  ) {
    String domainName = domainSpec.name();
    String domainClassName = getDefaultDomainClassName(domainName, false, cfg);
    return imports.addAndGetSimpleName(domainClassName);
  }

  static String buildChannelDeclarationByConstraints(
      SpecificationContext context,
      List<ConstraintSpecification> constraints,
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    Map<TraversePathSpecification, Equivalence> equivalenceIndex = makeEquivalenceIndex(constraints);
    TraversePathSpecification pathFromThisToDomain = getPathFromThisToDomain();
    Equivalence equivalence = equivalenceIndex.get(pathFromThisToDomain);
    if (equivalence != null) {
      for (TraversePathSpecification equivalentPath : equivalence.equivalentPaths()) {
        String source = equivalentPath.sourceDomainAlias();
        List<ChannelSpecification> channels = getContextChannels(context, source);
        if (!channels.isEmpty()) {
          if (equivalentPath.transitions().size() == 1) {
            TraverseTransitionSpecification transition = equivalentPath.transitions().get(0);
            if (transition.isThruTransition()) {
              TraverseTransitionThruSpecification thruTransition = transition.asThruTransition();
              ChannelSpecification channel = channels.stream()
                  .filter(c -> thruTransition.channelAlias().equals(c.alias()))
                  .collect(tech.intellispaces.commons.stream.Collectors.one());
              if (channel.target().sideAlias() != null) {
                return channel.target().sideAlias();
              } else if (channel.target().instance() != null && channel.target().instance().isStringInstance()) {
                if (ReflectionsNodeFunctions.ontologyReference().isDomainOfDomains(channel.target().domainAlias())) {
                  return imports.addAndGetSimpleName(getDefaultDomainClassName(channel.target().instance().asString(), false, cfg));
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
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    return CollectionFunctions.mapEach(channelSpec.qualifiers(),
        qualifierChannel -> buildChannelQualifier(qualifierChannel, context, imports, cfg));
  }

  static Map<String, Object> buildChannelQualifier(
      ChannelSpecification qualifierChannel,
      SpecificationContext context,
      MutableDependencySet imports,
      Configuration cfg
  ) throws MojoExecutionException {
    var map = new HashMap<String, Object>();
    map.put("alias", qualifierChannel.alias());
    map.put("type", buildChannelSideTypeDeclaration(qualifierChannel.target(), context, imports, cfg, true));
    return map;
  }

  static boolean isDatasetDomain(
      DomainSpecification domainSpec, Configuration cfg
  ) throws MojoExecutionException {
    for (SuperDomainSpecification superDomainSpec : domainSpec.superDomains()) {
      String superDomainAlias = superDomainSpec.alias();
      DomainReference referenceDomain = ReflectionsNodeFunctions.ontologyReference().getDomainByName(superDomainAlias);
      if (referenceDomain != null && DomainAssignments.Dataset.is(referenceDomain.assignment())) {
          return true;
      }
      if (isDatasetDomain(findDomain(superDomainAlias, cfg), cfg)) {
        return true;
      }
    }
    return false;
  }

  static boolean isTypeRelatedChannel(ChannelSpecification channelSpec) {
    if (channelSpec.target().domainAlias() != null) {
      if (ReflectionsNodeFunctions.ontologyReference().isDomainOfDomains(channelSpec.target().domainAlias())) {
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

  static String buildAllowedTraverse(List<AllowedTraverseType> allowedTraverses, MutableDependencySet imports) {
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
      String domainAlias, Configuration cfg
  ) throws MojoExecutionException {
    try {
      return cfg.repository().findDomain(domainAlias);
    } catch (SpecificationException e) {
      throw new MojoExecutionException("Could not find domain by alias '" + domainAlias + "'", e);
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

  static String getDomainClassName(DomainSpecification domainSpec, Configuration cfg) {
    return getDomainClassCanonicalName(domainSpec.name(), cfg);
  }

  static String getDomainClassName(String domainAlias, Configuration cfg) {
    return getDomainClassCanonicalName(domainAlias, cfg);
  }

  static String getDomainClassCanonicalName(String domainName, Configuration cfg) {
    return cfg.settings().basePackage() + domainName + "Domain";
  }

  static String getChannelClassName(ChannelSpecification channelSpec, Configuration cfg) {
    return NameConventionFunctions.convertToChannelClassName(cfg.settings().basePackage() + channelSpec.name());
  }

  static String getDefaultDomainClassName(String domainAlias, boolean enablePrimitives, Configuration cfg) {
    DomainReference domain = ReflectionsNodeFunctions.ontologyReference().getDomainByName(domainAlias);
    if (domain != null && domain.delegateClassName() != null) {
      if (enablePrimitives) {
        if (DomainAssignments.Boolean.is(domain.assignment())) {
          return boolean.class.getCanonicalName();
        } else if (DomainAssignments.Byte.is(domain.assignment())) {
          return byte.class.getCanonicalName();
        } else if (DomainAssignments.Short.is(domain.assignment())) {
          return short.class.getCanonicalName();
        } else if (DomainAssignments.Integer.is(domain.assignment())) {
          return int.class.getCanonicalName();
        } else if (DomainAssignments.Long.is(domain.assignment())) {
          return long.class.getCanonicalName();
        } else if (DomainAssignments.Float.is(domain.assignment())) {
          return float.class.getCanonicalName();
        } else if (DomainAssignments.Double.is(domain.assignment())) {
          return double.class.getCanonicalName();
        }
      }
      return domain.delegateClassName();
    }
    return getDomainClassCanonicalName(domainAlias, cfg);
  }

  static String getDomainOfDomainsName() {
    return ReflectionsNodeFunctions.ontologyReference().getDomainByType(DomainAssignments.Domain).domainName();
  }

  static String getDomainOfDomainsClassCanonicalName() {
    return ReflectionsNodeFunctions.ontologyReference().getDomainByType(DomainAssignments.Domain).delegateClassName();
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
      Configuration cfg, String canonicalName, String sourceCode
  ) throws MojoExecutionException {
    Path path = new File(StringFunctions.join(
        cfg.settings().outputDirectory(),
        canonicalName.replace(".", File.separator),
        File.separator
    ) + ".java").toPath();
    try {
      Files.createDirectories(path.getParent());
      Files.writeString(path, sourceCode);
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
