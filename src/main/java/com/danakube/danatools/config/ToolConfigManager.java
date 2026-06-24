package com.danakube.danatools.config;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomTool;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ToolConfigManager {

    private final DanaTools plugin;
    private final Map<String, CustomTool> tools = new HashMap<>();

    public ToolConfigManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void loadTools() {
        tools.clear();
        File folder = plugin.getConfigManager().getToolsFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                String id = config.getString("id");
                if (id == null) {
                    plugin.getLogger().warning("Fichier de configuration d'outil sans ID: " + file.getName());
                    continue;
                }

                String materialStr = config.getString("material");
                Material material = Material.matchMaterial(materialStr != null ? materialStr : "");
                if (material == null) {
                    plugin.getLogger().warning("Materiel invalide pour l'outil: " + id + " (" + materialStr + ")");
                    continue;
                }

                int customModelData = config.getInt("custom-model-data", 0);
                String displayName = config.getString("display-name", id);
                List<String> lore = config.getStringList("lore");

                int xpCurveBase = config.getInt("xp-curve.base", 100);
                double xpCurveMultiplier = config.getDouble("xp-curve.multiplier", 1.5);

                Map<Material, CustomTool.BlockActivity> blockActivities = new HashMap<>();
                ConfigurationSection activitiesSection = config.getConfigurationSection("block-activities");
                if (activitiesSection != null) {
                    for (String key : activitiesSection.getKeys(false)) {
                        Material oreMaterial = Material.matchMaterial(key);
                        if (oreMaterial != null) {
                            ConfigurationSection activitySec = activitiesSection.getConfigurationSection(key);
                            if (activitySec != null) {
                                int xp = activitySec.getInt("xp", 0);
                                CustomTool.CoreDrop coreDrop = null;
                                ConfigurationSection dropSec = activitySec.getConfigurationSection("core-drop");
                                if (dropSec != null) {
                                    String modifierId = dropSec.getString("modifier-id");
                                    double chancePercent = dropSec.getDouble("chance-percent", 0.0);
                                    if (modifierId != null) {
                                        coreDrop = new CustomTool.CoreDrop(modifierId, chancePercent);
                                    }
                                }
                                blockActivities.put(oreMaterial, new CustomTool.BlockActivity(xp, coreDrop));
                            }
                        } else {
                            plugin.getLogger().warning("Materiel invalide dans block-activities de l'outil " + id + ": " + key);
                        }
                    }
                }

                if (blockActivities.isEmpty()) {
                    ConfigurationSection xpSection = config.getConfigurationSection("xp-gain");
                    if (xpSection != null) {
                        for (String key : xpSection.getKeys(false)) {
                            Material oreMaterial = Material.matchMaterial(key);
                            if (oreMaterial != null) {
                                blockActivities.put(oreMaterial, new CustomTool.BlockActivity(xpSection.getInt(key), null));
                            } else {
                                plugin.getLogger().warning("Materiel invalide dans xp-gain de l'outil " + id + ": " + key);
                            }
                        }
                    }
                }

                int maxLevel = config.getInt("progression.max-level", 20);
                int maxSlots = config.getInt("progression.max-slots", 4);

                Map<Integer, Integer> slotsProgression = new HashMap<>();
                ConfigurationSection slotsSection = config.getConfigurationSection("progression.slots");
                if (slotsSection != null) {
                    for (String key : slotsSection.getKeys(false)) {
                        try {
                            int level = Integer.parseInt(key);
                            int slotsCount = slotsSection.getInt(key);
                            slotsProgression.put(level, slotsCount);
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Niveau invalide dans progression.slots de l'outil " + id + ": " + key);
                        }
                    }
                }

                Map<Enchantment, Integer> enchantmentLimits = new HashMap<>();
                ConfigurationSection limitsSection = config.getConfigurationSection("enchantment-limits");
                if (limitsSection != null) {
                    for (String key : limitsSection.getKeys(false)) {
                        Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
                        Enchantment enchantment = enchantmentRegistry.get(NamespacedKey.minecraft(key.toLowerCase()));
                        if (enchantment != null) {
                            enchantmentLimits.put(enchantment, limitsSection.getInt(key));
                        } else {
                            @SuppressWarnings("deprecation")
                            Enchantment legacyEnchant = Enchantment.getByName(key.toUpperCase());
                            if (legacyEnchant != null) {
                                enchantmentLimits.put(legacyEnchant, limitsSection.getInt(key));
                            } else {
                                plugin.getLogger().warning("Enchantement invalide dans enchantment-limits de l'outil " + id + ": " + key);
                            }
                        }
                    }
                }

                String noModifierMessage = config.getString("no-modifier-message", null);

                Map<String, Integer> allowedModifiers = new HashMap<>();
                ConfigurationSection allowedSec = config.getConfigurationSection("allowed-modifiers");
                if (allowedSec != null) {
                    for (String key : allowedSec.getKeys(false)) {
                        allowedModifiers.put(key, allowedSec.getInt(key));
                    }
                }

                CustomTool customTool = new CustomTool(
                        id, material, customModelData, displayName, lore,
                        xpCurveBase, xpCurveMultiplier, blockActivities,
                        maxLevel, slotsProgression, maxSlots, enchantmentLimits,
                        noModifierMessage, allowedModifiers
                );

                tools.put(id, customTool);
                plugin.getLogger().info("Outil charge : " + id);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Impossible de charger l'outil depuis le fichier: " + file.getName(), e);
            }
        }
    }

    public CustomTool getTool(String id) {
        return tools.get(id);
    }

    public Collection<CustomTool> getTools() {
        return tools.values();
    }
}
