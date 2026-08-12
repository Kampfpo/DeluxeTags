package me.clip.deluxetags.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Assume;
import org.junit.Test;

public class BukkitCompatTest {

  @Test
  public void safelySkipsModernItemMetaApisOnLegacyBukkit() {
    Assume.assumeFalse(hasMethod(ItemMeta.class, "setCustomModelData", Integer.class));

    ItemMeta itemMeta = proxy(ItemMeta.class, new HashMap<String, List<Object>>(), null);
    assertEquals(false, BukkitCompat.setItemModel(itemMeta, "minecraft:test"));
    assertEquals(false, BukkitCompat.setCustomModelData(itemMeta, 12));
    assertEquals(false, BukkitCompat.setCustomModelDataComponent(
        itemMeta,
        Collections.<Color>emptyList(),
        Collections.<Boolean>emptyList(),
        Collections.<Float>emptyList(),
        Collections.<String>emptyList()
    ));
  }

  @Test
  public void invokesModernItemMetaApisWhenAvailable() throws ClassNotFoundException {
    Assume.assumeTrue(hasMethod(ItemMeta.class, "setCustomModelData", Integer.class));

    Class<?> componentClass = Class.forName(
        "org.bukkit.inventory.meta.components.CustomModelDataComponent"
    );
    Map<String, List<Object>> componentCalls = new HashMap<>();
    Object component = proxy(componentClass, componentCalls, null);
    Map<String, List<Object>> itemMetaCalls = new HashMap<>();
    ItemMeta itemMeta = proxy(ItemMeta.class, itemMetaCalls, component);

    List<Color> colors = Collections.singletonList(Color.fromRGB(1, 2, 3));
    List<Boolean> flags = Collections.singletonList(true);
    List<Float> floats = Collections.singletonList(12.5F);
    List<String> strings = Collections.singletonList("variant");

    assertTrue(BukkitCompat.setItemModel(itemMeta, "minecraft:test"));
    assertTrue(BukkitCompat.setCustomModelData(itemMeta, 12));
    assertTrue(BukkitCompat.setCustomModelDataComponent(itemMeta, colors, flags, floats, strings));

    assertEquals("minecraft:test", firstArgument(itemMetaCalls, "setItemModel").toString());
    assertEquals(12, firstArgument(itemMetaCalls, "setCustomModelData"));
    assertSame(component, firstArgument(itemMetaCalls, "setCustomModelDataComponent"));
    assertSame(colors, firstArgument(componentCalls, "setColors"));
    assertSame(flags, firstArgument(componentCalls, "setFlags"));
    assertSame(floats, firstArgument(componentCalls, "setFloats"));
    assertSame(strings, firstArgument(componentCalls, "setStrings"));
  }

  private static boolean hasMethod(Class<?> type, String name, Class<?>... parameterTypes) {
    try {
      type.getMethod(name, parameterTypes);
      return true;
    } catch (NoSuchMethodException ignored) {
      return false;
    }
  }

  private static Object firstArgument(Map<String, List<Object>> calls, String method) {
    return calls.get(method).get(0);
  }

  @SuppressWarnings("unchecked")
  private static <T> T proxy(
      Class<T> type,
      Map<String, List<Object>> calls,
      Object customModelDataComponent
  ) {
    return (T) Proxy.newProxyInstance(
        type.getClassLoader(),
        new Class<?>[]{type},
        (proxy, method, arguments) -> invokeProxy(proxy, method, arguments, calls, customModelDataComponent)
    );
  }

  private static Object invokeProxy(
      Object proxy,
      Method method,
      Object[] arguments,
      Map<String, List<Object>> calls,
      Object customModelDataComponent
  ) {
    if (method.getName().equals("getCustomModelDataComponent")) {
      return customModelDataComponent;
    }
    if (method.getName().equals("toString")) {
      return "BukkitCompatTestProxy";
    }
    if (method.getName().equals("hashCode")) {
      return System.identityHashCode(proxy);
    }
    if (method.getName().equals("equals")) {
      return proxy == arguments[0];
    }

    List<Object> methodArguments = calls.computeIfAbsent(method.getName(), key -> new ArrayList<>());
    if (arguments != null) {
      Collections.addAll(methodArguments, arguments);
    }

    return defaultValue(method.getReturnType());
  }

  private static Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) {
      return null;
    }
    if (type == boolean.class) {
      return false;
    }
    if (type == char.class) {
      return '\0';
    }
    return 0;
  }
}
