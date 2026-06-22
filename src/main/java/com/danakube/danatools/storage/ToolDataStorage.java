package com.danakube.danatools.storage;

import com.danakube.danatools.DanaTools;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolDataStorage {

    private static final NamespacedKey KEY_ID = new NamespacedKey(DanaTools.getInstance(), "id");
    private static final NamespacedKey KEY_XP = new NamespacedKey(DanaTools.getInstance(), "xp");
    private static final NamespacedKey KEY_LEVEL = new NamespacedKey(DanaTools.getInstance(), "level");
    private static final NamespacedKey KEY_SLOTS_TOTAL = new NamespacedKey(DanaTools.getInstance(), "slots_total");
    private static final NamespacedKey KEY_SLOTS_USED = new NamespacedKey(DanaTools.getInstance(), "slots_used");
    private static final NamespacedKey KEY_MODIFIERS = new NamespacedKey(DanaTools.getInstance(), "modifiers");

    public static boolean isDanaTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(KEY_ID, PersistentDataType.STRING);
    }

    public static String getToolId(ItemStack item) {
        if (!isDanaTool(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(KEY_ID, PersistentDataType.STRING);
    }

    public static int getXp(ItemStack item) {
        if (!isDanaTool(item)) return 0;
        Integer xp = item.getItemMeta().getPersistentDataContainer().get(KEY_XP, PersistentDataType.INTEGER);
        return xp != null ? xp : 0;
    }

    public static void setXp(ItemStack item, int xp) {
        if (!isDanaTool(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_XP, PersistentDataType.INTEGER, xp);
        item.setItemMeta(meta);
    }

    public static int getLevel(ItemStack item) {
        if (!isDanaTool(item)) return 1;
        Integer level = item.getItemMeta().getPersistentDataContainer().get(KEY_LEVEL, PersistentDataType.INTEGER);
        return level != null ? level : 1;
    }

    public static void setLevel(ItemStack item, int level) {
        if (!isDanaTool(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_LEVEL, PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }

    public static int getSlotsTotal(ItemStack item) {
        if (!isDanaTool(item)) return 0;
        Integer slots = item.getItemMeta().getPersistentDataContainer().get(KEY_SLOTS_TOTAL, PersistentDataType.INTEGER);
        return slots != null ? slots : 0;
    }

    public static void setSlotsTotal(ItemStack item, int slotsTotal) {
        if (!isDanaTool(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_SLOTS_TOTAL, PersistentDataType.INTEGER, slotsTotal);
        item.setItemMeta(meta);
    }

    public static int getSlotsUsed(ItemStack item) {
        if (!isDanaTool(item)) return 0;
        Integer slots = item.getItemMeta().getPersistentDataContainer().get(KEY_SLOTS_USED, PersistentDataType.INTEGER);
        return slots != null ? slots : 0;
    }

    public static void setSlotsUsed(ItemStack item, int slotsUsed) {
        if (!isDanaTool(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_SLOTS_USED, PersistentDataType.INTEGER, slotsUsed);
        item.setItemMeta(meta);
    }

    public static List<String> getModifiers(ItemStack item) {
        if (!isDanaTool(item)) return new ArrayList<>();
        String mods = item.getItemMeta().getPersistentDataContainer().get(KEY_MODIFIERS, PersistentDataType.STRING);
        if (mods == null || mods.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(mods.split(",")));
    }

    public static void addModifier(ItemStack item, String modifierId) {
        if (!isDanaTool(item)) return;
        List<String> modifiers = getModifiers(item);
        if (!modifiers.contains(modifierId)) {
            modifiers.add(modifierId);
            String joined = String.join(",", modifiers);
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(KEY_MODIFIERS, PersistentDataType.STRING, joined);
            item.setItemMeta(meta);
        }
    }

    public static void initToolData(ItemStack item, String toolId, int slotsTotal) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_ID, PersistentDataType.STRING, toolId);
        pdc.set(KEY_XP, PersistentDataType.INTEGER, 0);
        pdc.set(KEY_LEVEL, PersistentDataType.INTEGER, 1);
        pdc.set(KEY_SLOTS_TOTAL, PersistentDataType.INTEGER, slotsTotal);
        pdc.set(KEY_SLOTS_USED, PersistentDataType.INTEGER, 0);
        pdc.set(KEY_MODIFIERS, PersistentDataType.STRING, "");
        item.setItemMeta(meta);
    }
}
