package me.clip.deluxetags.utils;

import java.lang.reflect.Method;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Access to Bukkit APIs added after the plugin's 1.8.8 baseline.
 *
 * <p>Keeping the newer types out of method descriptors allows this class, and
 * callers such as {@code TagConfig}, to be loaded by legacy servers.</p>
 */
public final class BukkitCompat {

  private BukkitCompat() {
  }

  public static void setComments(FileConfiguration config, String path, List<String> comments) {
    try {
      Method setComments = config.getClass().getMethod("setComments", String.class, List.class);
      setComments.invoke(config, path, comments);
    } catch (ReflectiveOperationException ignored) {
      // Section comments are cosmetic and were not available on legacy Bukkit.
    }
  }

  public static boolean setItemModel(ItemMeta itemMeta, String value) {
    try {
      Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
      Object namespacedKey = namespacedKeyClass.getMethod("fromString", String.class).invoke(null, value);
      if (namespacedKey == null) {
        return false;
      }

      ItemMeta.class.getMethod("setItemModel", namespacedKeyClass).invoke(itemMeta, namespacedKey);
      return true;
    } catch (ReflectiveOperationException ignored) {
      return false;
    }
  }

  public static boolean setCustomModelData(ItemMeta itemMeta, int value) {
    try {
      ItemMeta.class.getMethod("setCustomModelData", Integer.class).invoke(itemMeta, value);
      return true;
    } catch (ReflectiveOperationException ignored) {
      return false;
    }
  }

  public static boolean setCustomModelDataComponent(
      ItemMeta itemMeta,
      List<Color> colors,
      List<Boolean> flags,
      List<Float> floats,
      List<String> strings
  ) {
    try {
      Class<?> componentClass = Class.forName(
          "org.bukkit.inventory.meta.components.CustomModelDataComponent"
      );
      Object component = ItemMeta.class.getMethod("getCustomModelDataComponent").invoke(itemMeta);

      if (!colors.isEmpty()) {
        componentClass.getMethod("setColors", List.class).invoke(component, colors);
      }
      if (!flags.isEmpty()) {
        componentClass.getMethod("setFlags", List.class).invoke(component, flags);
      }
      if (!floats.isEmpty()) {
        componentClass.getMethod("setFloats", List.class).invoke(component, floats);
      }
      if (!strings.isEmpty()) {
        componentClass.getMethod("setStrings", List.class).invoke(component, strings);
      }

      ItemMeta.class.getMethod("setCustomModelDataComponent", componentClass).invoke(itemMeta, component);
      return true;
    } catch (ReflectiveOperationException ignored) {
      return false;
    }
  }

  public static void setSkullOwner(SkullMeta skullMeta, OfflinePlayer player) {
    try {
      SkullMeta.class.getMethod("setOwningPlayer", OfflinePlayer.class).invoke(skullMeta, player);
    } catch (ReflectiveOperationException ignored) {
      skullMeta.setOwner(player.getName());
    }
  }
}
