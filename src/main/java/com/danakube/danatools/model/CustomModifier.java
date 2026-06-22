package com.danakube.danatools.model;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomModifier {
    private final String id;
    private final String displayName;
    private final List<String> lore;

    private final Material templateMaterial;
    private final int templateCustomModelData;
    private final String templateDisplayName;

    private final Material ingredientMaterial;
    private final int ingredientCustomModelData;
    private final String ingredientDisplayName;

    private final int minToolLevel;
    private final int slotCost;
    private final List<String> compatibleTools;
    private final List<String> incompatibleModifiers;

    private final String behaviorType;
    private final Map<String, Object> behaviorSettings;

    public CustomModifier(String id, String displayName, List<String> lore,
                          Material templateMaterial, int templateCustomModelData, String templateDisplayName,
                          Material ingredientMaterial, int ingredientCustomModelData, String ingredientDisplayName,
                          int minToolLevel, int slotCost, List<String> compatibleTools, List<String> incompatibleModifiers,
                          String behaviorType, Map<String, Object> behaviorSettings) {
        this.id = id;
        this.displayName = displayName;
        this.lore = lore;
        this.templateMaterial = templateMaterial;
        this.templateCustomModelData = templateCustomModelData;
        this.templateDisplayName = templateDisplayName;
        this.ingredientMaterial = ingredientMaterial;
        this.ingredientCustomModelData = ingredientCustomModelData;
        this.ingredientDisplayName = ingredientDisplayName;
        this.minToolLevel = minToolLevel;
        this.slotCost = slotCost;
        this.compatibleTools = compatibleTools;
        this.incompatibleModifiers = incompatibleModifiers;
        this.behaviorType = behaviorType;
        this.behaviorSettings = behaviorSettings != null ? behaviorSettings : new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
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

    public int getMinToolLevel() {
        return minToolLevel;
    }

    public int getSlotCost() {
        return slotCost;
    }

    public List<String> getCompatibleTools() {
        return compatibleTools;
    }

    public List<String> getIncompatibleModifiers() {
        return incompatibleModifiers;
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
