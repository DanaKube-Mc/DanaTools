package com.danakube.danatools.config;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ModifierConfigManager {

    private final DanaTools plugin;
    private final Map<String, CustomModifier> modifiers = new HashMap<>();

    public ModifierConfigManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void loadModifiers() {
        modifiers.clear();
        File folder = plugin.getConfigManager().getModifiersFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                String id = config.getString("id");
                if (id == null) {
                    plugin.getLogger().warning("Fichier de configuration de modificateur sans ID: " + file.getName());
                    continue;
                }

                String displayName = config.getString("display-name", id);
                List<String> lore = config.getStringList("lore");

                String templateMatStr = config.getString("template-item.material");
                Material templateMaterial = Material.matchMaterial(templateMatStr != null ? templateMatStr : "");
                if (templateMaterial == null) {
                    plugin.getLogger().warning("Materiel de template invalide pour le modificateur: " + id);
                    continue;
                }
                int templateCustomModelData = config.getInt("template-item.custom-model-data", 0);
                String templateDisplayName = config.getString("template-item.display-name", displayName);

                String ingredientMatStr = config.getString("ingredient-item.material");
                Material ingredientMaterial = Material.matchMaterial(ingredientMatStr != null ? ingredientMatStr : "");
                if (ingredientMaterial == null) {
                    plugin.getLogger().warning("Materiel d'ingredient invalide pour le modificateur: " + id);
                    continue;
                }
                int ingredientCustomModelData = config.getInt("ingredient-item.custom-model-data", 0);
                String ingredientDisplayName = config.getString("ingredient-item.display-name");

                int minToolLevel = config.getInt("conditions.min-tool-level", 1);
                int slotCost = config.getInt("conditions.slot-cost", 1);
                List<String> compatibleTools = config.getStringList("conditions.compatible-tools");
                List<String> incompatibleModifiers = config.getStringList("conditions.incompatible-modifiers");

                String behaviorType = config.getString("behavior.type", "NONE");
                Map<String, Object> behaviorSettings = new HashMap<>();
                ConfigurationSection behaviorSection = config.getConfigurationSection("behavior");
                if (behaviorSection != null) {
                    for (String key : behaviorSection.getKeys(false)) {
                        if (!key.equals("type")) {
                            behaviorSettings.put(key, behaviorSection.get(key));
                        }
                    }
                }

                CustomModifier customModifier = new CustomModifier(
                        id, displayName, lore,
                        templateMaterial, templateCustomModelData, templateDisplayName,
                        ingredientMaterial, ingredientCustomModelData, ingredientDisplayName,
                        minToolLevel, slotCost, compatibleTools, incompatibleModifiers,
                        behaviorType, behaviorSettings
                );

                modifiers.put(id, customModifier);
                plugin.getLogger().info("Modificateur charge : " + id);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Impossible de charger le modificateur depuis le fichier: " + file.getName(), e);
            }
        }
    }

    public CustomModifier getModifier(String id) {
        return modifiers.get(id);
    }

    public Collection<CustomModifier> getModifiers() {
        return modifiers.values();
    }
}
