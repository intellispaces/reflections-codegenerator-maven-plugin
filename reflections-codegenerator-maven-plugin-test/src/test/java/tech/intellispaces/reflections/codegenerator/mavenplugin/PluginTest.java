package tech.intellispaces.reflections.codegenerator.mavenplugin;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import tech.intellispaces.reflections.framework.annotation.Channel;
import tech.intellispaces.reflections.framework.annotation.Domain;
import tech.intellispaces.reflections.framework.traverse.TraverseTypes;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The plugin integration test.
 */
public class PluginTest {

  @Test
  public void testEmptyDomain() throws Exception {
    Class<?> emptyDomainClass = Class.forName("tech.test.intellispaces.plugin.sample.domain.EmptyDomain");

    assertThat(emptyDomainClass.isInterface()).isTrue();
    assertThat(emptyDomainClass.getAnnotation(Domain.class).value()).isEqualTo("9938abbf-2778-402b-8699-61afb0497e80");
    assertThat(emptyDomainClass.getAnnotation(Domain.class).name()).isEqualTo("intellispaces.plugin.sample.domain.Empty");
    assertThat(emptyDomainClass.getDeclaredMethods()).isEmpty();
  }

  @Test
  public void testPrimitiveDomain() throws Exception {
    Class<?> emptyDomainClass = Class.forName("tech.test.intellispaces.plugin.sample.domain.PrimitiveDomain");

    assertThat(emptyDomainClass.isInterface()).isTrue();
    assertThat(emptyDomainClass.getAnnotation(Domain.class).value()).isEqualTo("cf2db025-febc-427e-ad15-b8df0b98c4ea");
    assertThat(emptyDomainClass.getAnnotation(Domain.class).name()).isEqualTo("intellispaces.plugin.sample.domain.Primitive");

    assertThat(emptyDomainClass.getDeclaredMethods()).hasSize(1);
    Method method = emptyDomainClass.getDeclaredMethods()[0];
    assertThat(method.getName()).isEqualTo("length");
    assertThat(method.getReturnType()).isSameAs(int.class);
    assertThat(method.getAnnotation(Channel.class).value()).isEqualTo("fb17bec4-0d1a-4c1d-b311-23bd9cde2f45");
    assertThat(method.getAnnotation(Channel.class).allowedTraverse()).containsExactly(TraverseTypes.Mapping);
  }
}
