package com.danakube.danatools.model;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomModifier {
    private final String id;
    private final List<String> incompatibleModifiers;

    private final Material templateMaterial;
    private final int templateCustomModelData;
    private final String templateDisplayName;

    private final Material ingredientMaterial;
    private final int ingredientCustomModelData;
    private final String ingredientDisplayName;
    private final String ingredientTexture;

    private final List<String> compatibleTools;

    private final Map<Integer, LevelSettings> levels;

    public CustomModifier(String id, List<String> incompatibleModifiers,
                          Material templateMaterial, int templateCustomModelData, String templateDisplayName,
                          Material ingredientMaterial, int ingredientCustomModelData, String ingredientDisplayName,
                          String ingredientTexture, List<String> compatibleTools,
                          Map<Integer, LevelSettings> levels) {
        this.id = id;
        this.incompatibleModifiers = incompatibleModifiers;
        this.templateMaterial = templateMaterial;
        this.templateCustomModelData = templateCustomModelData;
        this.templateDisplayName = templateDisplayName;
        this.ingredientMaterial = ingredientMaterial;
        this.ingredientCustomModelData = ingredientCustomModelData;
        this.ingredientDisplayName = ingredientDisplayName;
        this.ingredientTexture = ingredientTexture;
        this.compatibleTools = compatibleTools;
        this.levels = levels != null ? levels : new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public List<String> getIncompatibleModifiers() {
        return incompatibleModifiers;
    }

    public Material getTemplateMaterial() {
        return templateMaterial;
    }

    public int getTemplateCustomModelData() {
        return templateCustomModelData;
    }

    public String getTemplateDisplayName() {
        return templateDisplayName;
    }

    public Material getIngredientMaterial() {
        return ingredientMaterial;
    }

    public int getIngredientCustomModelData() {
        return ingredientCustomModelData;
    }

    public String getIngredientDisplayName() {
        return ingredientDisplayName;
    }

    public String getIngredientTexture() {
        return ingredientTexture;
    }

    public List<String> getCompatibleTools() {
        return compatibleTools;
    }

    public Map<Integer, LevelSettings> getLevels() {
        return levels;
    }

    public LevelSettings getLevel(int level) {
        return levels.get(level);
    }

    public boolean hasLevel(int level) {
        return levels.containsKey(level);
    }

    public int getMaxLevel() {
        return levels.keySet().stream().max(Integer::compare).orElse(0);
    }

    public static class LevelSettings {
        private final int level;
        private final String displayName;
        private final List<String> lore;
        private final int minToolLevel;
        private final int slotCost;
        private final String behaviorType;
        private final Map<String, Object> behaviorSettings;

        public LevelSettings(int level, String displayName, List<String> lore, int minToolLevel, int slotCost,
                             String behaviorType, Map<String, Object> behaviorSettings) {
            this.level = level;
            this.displayName = displayName;
            this.lore = lore;
            this.minToolLevel = minToolLevel;
            this.slotCost = slotCost;
            this.behaviorType = behaviorType;
            this.behaviorSettings = behaviorSettings != null ? behaviorSettings : new HashMap<>();
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getLore() {
            return lore;
        }

        public int getMinToolLevel() {
            return minToolLevel;
        }

        public int getSlotCost() {
            return slotCost;
        }

        public String getBehaviorType() {
            return behaviorType;
        }

        public Map<String, Object> getBehaviorSettings() {
            return behaviorSettings;
        }

        public int getBehaviorInt(String key, int defaultValue) {
            Object obj = behaviorSettings.get(key);
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            return defaultValue;
        }
    }
}
