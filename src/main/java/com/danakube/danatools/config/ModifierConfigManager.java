package com.danakube.danatools.config;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Bukkit;


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

                List<String> incompatibleModifiers = config.getStringList("incompatible-modifiers");
                if (incompatibleModifiers.isEmpty()) {
                    incompatibleModifiers = config.getStringList("conditions.incompatible-modifiers");
                }

                String templateMatStr = config.getString("template-item.material");
                Material templateMaterial = Material.matchMaterial(templateMatStr != null ? templateMatStr : "");
                if (templateMaterial == null) {
                    plugin.getLogger().warning("Materiel de template invalide pour le modificateur: " + id);
                    continue;
                }
                int templateCustomModelData = config.getInt("template-item.custom-model-data", 0);
                String templateDisplayName = config.getString("template-item.display-name", id);

                String ingredientMatStr = config.getString("ingredient-item.material");
                Material ingredientMaterial = Material.matchMaterial(ingredientMatStr != null ? ingredientMatStr : "");
                if (ingredientMaterial == null) {
                    plugin.getLogger().warning("Materiel d'ingredient invalide pour le modificateur: " + id);
                    continue;
                }
                int ingredientCustomModelData = config.getInt("ingredient-item.custom-model-data", 0);
                String ingredientDisplayName = config.getString("ingredient-item.display-name");
                String ingredientTexture = config.getString("ingredient-item.texture");

                List<String> compatibleTools = config.getStringList("compatible-tools");
                if (compatibleTools.isEmpty()) {
                    compatibleTools = config.getStringList("conditions.compatible-tools");
                }

                Map<Integer, CustomModifier.LevelSettings> levelsMap = new HashMap<>();
                ConfigurationSection levelsSection = config.getConfigurationSection("levels");
                if (levelsSection != null) {
                    for (String key : levelsSection.getKeys(false)) {
                        try {
                            int levelNum = Integer.parseInt(key);
                            ConfigurationSection lvlSec = levelsSection.getConfigurationSection(key);
                            if (lvlSec != null) {
                                String lvlDisplayName = lvlSec.getString("display-name", id);
                                List<String> lvlLore = lvlSec.getStringList("lore");
                                int lvlSlotCost = lvlSec.getInt("slot-cost", 1);
                                int lvlMinToolLevel = lvlSec.getInt("min-tool-level", 1);

                                String lvlBehaviorType = lvlSec.getString("behavior.type", "NONE");
                                Map<String, Object> lvlBehaviorSettings = new HashMap<>();
                                ConfigurationSection lvlBehaviorSec = lvlSec.getConfigurationSection("behavior");
                                if (lvlBehaviorSec != null) {
                                    for (String bKey : lvlBehaviorSec.getKeys(false)) {
                                        if (!bKey.equals("type")) {
                                            lvlBehaviorSettings.put(bKey, lvlBehaviorSec.get(bKey));
                                        }
                                    }
                                }

                                ConfigurationSection propertiesSec = lvlSec.getConfigurationSection("properties");
                                if (propertiesSec != null) {
                                    for (String pKey : propertiesSec.getKeys(false)) {
                                        lvlBehaviorSettings.put(pKey, propertiesSec.get(pKey));
                                    }
                                }

                                CustomModifier.LevelSettings lvlSettings = new CustomModifier.LevelSettings(
                                        levelNum, lvlDisplayName, lvlLore, lvlMinToolLevel, lvlSlotCost,
                                        lvlBehaviorType, lvlBehaviorSettings
                                );
                                levelsMap.put(levelNum, lvlSettings);
                            }
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Niveau invalide dans levels pour " + id + ": " + key);
                        }
                    }
                }

                CustomModifier customModifier = new CustomModifier(
                        id, incompatibleModifiers,
                        templateMaterial, templateCustomModelData, templateDisplayName,
                        ingredientMaterial, ingredientCustomModelData, ingredientDisplayName,
                        ingredientTexture, compatibleTools, levelsMap
                );

                modifiers.put(id, customModifier);
                plugin.getLogger().info("Modificateur charge (avec niveaux) : " + id);

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

    public ItemStack buildIngredientItem(CustomModifier modifier) {
        if (modifier == null) return null;
        ItemStack ingredient;
        if (modifier.getIngredientMaterial() == Material.PLAYER_HEAD && modifier.getIngredientTexture() != null && !modifier.getIngredientTexture().isEmpty()) {
            ingredient = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sMeta = (SkullMeta) ingredient.getItemMeta();
            if (sMeta != null) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.getProperties().add(new ProfileProperty("textures", modifier.getIngredientTexture()));
                sMeta.setPlayerProfile(profile);
                ingredient.setItemMeta(sMeta);
            }
        } else {
            ingredient = new ItemStack(modifier.getIngredientMaterial());
        }

        ItemMeta iMeta = ingredient.getItemMeta();
        if (iMeta != null) {
            iMeta.displayName(DanaItemInstance.parseColor(modifier.getIngredientDisplayName()));
            if (modifier.getIngredientCustomModelData() > 0) {
                iMeta.setCustomModelData(modifier.getIngredientCustomModelData());
            }
            ingredient.setItemMeta(iMeta);
        }
        return ingredient;
    }
}
