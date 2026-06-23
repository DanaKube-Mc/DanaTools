package com.danakube.danatools.model;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomTool {
    private final String id;
    private final Material material;
    private final int customModelData;
    private final String displayName;
    private final List<String> lore;
    
    private final int xpCurveBase;
    private final double xpCurveMultiplier;
    
    private final Map<Material, Integer> xpGain;
    
    private final int maxLevel;
    private final Map<Integer, Integer> slotsProgression;
    private final int maxSlots;
    private final Map<Enchantment, Integer> enchantmentLimits;

    public CustomTool(String id, Material material, int customModelData, String displayName, List<String> lore,
                      int xpCurveBase, double xpCurveMultiplier, Map<Material, Integer> xpGain,
                      int maxLevel, Map<Integer, Integer> slotsProgression, int maxSlots,
                      Map<Enchantment, Integer> enchantmentLimits) {
        this.id = id;
        this.material = material;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore;
        this.xpCurveBase = xpCurveBase;
        this.xpCurveMultiplier = xpCurveMultiplier;
        this.xpGain = xpGain != null ? xpGain : new HashMap<>();
        this.maxLevel = maxLevel;
        this.slotsProgression = slotsProgression != null ? slotsProgression : new HashMap<>();
        this.maxSlots = maxSlots;
        this.enchantmentLimits = enchantmentLimits != null ? enchantmentLimits : new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getXpCurveBase() {
        return xpCurveBase;
    }

    public double getXpCurveMultiplier() {
        return xpCurveMultiplier;
    }

    public Map<Material, Integer> getXpGain() {
        return xpGain;
    }

    public int getXpForBlock(Material material) {
        return xpGain.getOrDefault(material, 0);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Map<Integer, Integer> getSlotsProgression() {
        return slotsProgression;
    }

    public int getSlotsForLevel(int level) {
        int slots = 0;
        int highestLevelSoFar = 0;
        for (Map.Entry<Integer, Integer> entry : slotsProgression.entrySet()) {
            int entryLevel = entry.getKey();
            if (level >= entryLevel && entryLevel > highestLevelSoFar) {
                slots = entry.getValue();
                highestLevelSoFar = entryLevel;
            }
        }
        return Math.min(slots, maxSlots);
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public Map<Enchantment, Integer> getEnchantmentLimits() {
        return enchantmentLimits;
    }

    public int getEnchantmentLimit(Enchantment enchantment) {
        return enchantmentLimits.getOrDefault(enchantment, enchantment.getMaxLevel());
    }
}
