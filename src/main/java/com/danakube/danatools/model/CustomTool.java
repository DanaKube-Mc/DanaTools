package com.danakube.danatools.model;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
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
    
    private final Map<Material, BlockActivity> blockActivities;
    private final BlockActivity defaultBlockActivity;
    private final FishingActivity fishingActivity;
    
    private final int maxLevel;
    private final Map<Integer, Integer> slotsProgression;
    private final int maxSlots;
    private final Map<Enchantment, Integer> enchantmentLimits;
    private final String noModifierMessage;
    private final Map<String, Integer> allowedModifiers;
    private final double xpGainDamageMultiplier;
    private final double xpGainMovementMultiplier;
    
    private final Map<EntityType, Integer> mobActivities;
    private final int defaultMobXp;
    private final boolean hasDefaultMobXp;

    public CustomTool(String id, Material material, int customModelData, String displayName, List<String> lore,
                      int xpCurveBase, double xpCurveMultiplier, Map<Material, BlockActivity> blockActivities,
                      BlockActivity defaultBlockActivity, FishingActivity fishingActivity,
                      int maxLevel, Map<Integer, Integer> slotsProgression, int maxSlots,
                      Map<Enchantment, Integer> enchantmentLimits, String noModifierMessage,
                      Map<String, Integer> allowedModifiers,
                      double xpGainDamageMultiplier, double xpGainMovementMultiplier,
                      Map<EntityType, Integer> mobActivities, int defaultMobXp, boolean hasDefaultMobXp) {
        this.id = id;
        this.material = material;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore;
        this.xpCurveBase = xpCurveBase;
        this.xpCurveMultiplier = xpCurveMultiplier;
        this.blockActivities = blockActivities != null ? blockActivities : new HashMap<>();
        this.defaultBlockActivity = defaultBlockActivity;
        this.fishingActivity = fishingActivity;
        this.maxLevel = maxLevel;
        this.slotsProgression = slotsProgression != null ? slotsProgression : new HashMap<>();
        this.maxSlots = maxSlots;
        this.enchantmentLimits = enchantmentLimits != null ? enchantmentLimits : new HashMap<>();
        this.noModifierMessage = noModifierMessage;
        this.allowedModifiers = allowedModifiers != null ? allowedModifiers : new HashMap<>();
        this.xpGainDamageMultiplier = xpGainDamageMultiplier;
        this.xpGainMovementMultiplier = xpGainMovementMultiplier;
        this.mobActivities = mobActivities != null ? mobActivities : new HashMap<>();
        this.defaultMobXp = defaultMobXp;
        this.hasDefaultMobXp = hasDefaultMobXp;
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
        Map<Material, Integer> xpGain = new HashMap<>();
        for (Map.Entry<Material, BlockActivity> entry : blockActivities.entrySet()) {
            xpGain.put(entry.getKey(), entry.getValue().getXp());
        }
        return xpGain;
    }

    public int getXpForBlock(Material material) {
        BlockActivity activity = blockActivities.get(material);
        return activity != null ? activity.getXp() : 0;
    }

    public BlockActivity getBlockActivity(Material material) {
        return blockActivities.get(material);
    }

    public FishingActivity getFishingActivity() {
        return fishingActivity;
    }

    public double getXpGainDamageMultiplier() {
        return xpGainDamageMultiplier;
    }

    public double getXpGainMovementMultiplier() {
        return xpGainMovementMultiplier;
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

    public String getNoModifierMessage() {
        return noModifierMessage;
    }

    public Map<String, Integer> getAllowedModifiers() {
        return allowedModifiers;
    }

    public BlockActivity getDefaultBlockActivity() {
        return defaultBlockActivity;
    }

    public boolean hasDefaultBlockActivity() {
        return defaultBlockActivity != null;
    }

    public Map<EntityType, Integer> getMobActivities() {
        return mobActivities;
    }

    public int getDefaultMobXp() {
        return defaultMobXp;
    }

    public boolean hasDefaultMobXp() {
        return hasDefaultMobXp;
    }

    public int getXpForMob(EntityType entityType) {
        if (mobActivities.containsKey(entityType)) {
            return mobActivities.get(entityType);
        }
        if (hasDefaultMobXp) {
            return defaultMobXp;
        }
        return 0;
    }

    public static class BlockActivity {
        private final int xp;
        private final CoreDrop coreDrop;

        public BlockActivity(int xp, CoreDrop coreDrop) {
            this.xp = xp;
            this.coreDrop = coreDrop;
        }

        public int getXp() {
            return xp;
        }

        public CoreDrop getCoreDrop() {
            return coreDrop;
        }

        public boolean hasCoreDrop() {
            return coreDrop != null;
        }
    }

    public static class CoreDrop {
        private final String modifierId;
        private final double chancePercent;

        public CoreDrop(String modifierId, double chancePercent) {
            this.modifierId = modifierId;
            this.chancePercent = chancePercent;
        }

        public String getModifierId() {
            return modifierId;
        }

        public double getChancePercent() {
            return chancePercent;
        }
    }

    public static class FishingActivity {
        private final int xp;
        private final CoreDrop coreDrop;

        public FishingActivity(int xp, CoreDrop coreDrop) {
            this.xp = xp;
            this.coreDrop = coreDrop;
        }

        public int getXp() {
            return xp;
        }

        public CoreDrop getCoreDrop() {
            return coreDrop;
        }

        public boolean hasCoreDrop() {
            return coreDrop != null;
        }
    }
}
