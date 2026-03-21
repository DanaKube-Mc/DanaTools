package com.babydev.danatools.listeners;

import com.babydev.danatools.DanaTools;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ToolEffectListener implements Listener {

    private final DanaTools plugin;
    private final Map<UUID, Set<PotionEffectType>> appliedEffects = new HashMap<>();

    public ToolEffectListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        updateEffects(player, newItem);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updateEffects(player, player.getInventory().getItemInMainHand());
        }, 1L);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        updateEffects(event.getPlayer(), event.getMainHandItem());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updateEffects(player, player.getInventory().getItemInMainHand());
            }, 1L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeCurrentEffects(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        removeCurrentEffects(event.getEntity());
    }

    /**
     * Met à jour les effets de potion du joueur en fonction de l'item tenu.
     */
    public void updateEffects(Player player, ItemStack item) {
        removeCurrentEffects(player);

        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        String toolId = meta.getPersistentDataContainer().get(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING);
        if (toolId == null)
            return;

        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        int level = plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId);
        Map<PotionEffectType, Integer> effectsToApply = new HashMap<>();

        ConfigurationSection levels = toolConfig.getConfigurationSection("levels");
        if (levels != null) {
            ConfigurationSection levelConfig = null;
            for (int i = level; i >= 0; i--) {
                ConfigurationSection section = levels.getConfigurationSection(String.valueOf(i));
                if (section != null) {
                    levelConfig = section;
                    break;
                }
            }

            if (levelConfig != null && levelConfig.contains("effects")) {
                ConfigurationSection section = levelConfig.getConfigurationSection("effects");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        String eName = section.getString(key + ".effect", "");
                        int eLevel = section.getInt(key + ".level", 1);
                        PotionEffectType type = PotionEffectType.getByName(eName.toUpperCase());
                        if (type != null) {
                            effectsToApply.put(type, Math.max(effectsToApply.getOrDefault(type, 0), eLevel - 1));
                        }
                    }
                }
            }
        }

        if (toolConfig.getBoolean("use_skills", false) && toolConfig.contains("skills")) {
            ConfigurationSection skillsSection = toolConfig.getConfigurationSection("skills");
            if (skillsSection != null) {
                for (String skillId : skillsSection.getKeys(false)) {
                    int skillLevelBought = plugin.getPlayerStatsManager().getSkillLevel(player.getUniqueId(), toolId,
                            skillId);
                    if (skillLevelBought > 0) {
                        ConfigurationSection skill = skillsSection.getConfigurationSection(skillId);
                        if (skill != null && skill.contains("effects")) {
                            ConfigurationSection skillEffects = skill.getConfigurationSection("effects");
                            if (skillEffects != null) {
                                for (String efKey : skillEffects.getKeys(false)) {
                                    String efName = skillEffects.getString(efKey + ".effect", "");
                                    int baseLvl = skillEffects.getInt(efKey + ".level", 0);
                                    int perLvl = skillEffects.getInt(efKey + ".level_up", 1);
                                    int finalLvl = baseLvl + (skillLevelBought - 1) * perLvl;

                                    PotionEffectType type = PotionEffectType.getByName(efName.toUpperCase());
                                    if (type != null && finalLvl > 0) {
                                        effectsToApply.put(type,
                                                Math.max(effectsToApply.getOrDefault(type, 0), finalLvl - 1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!effectsToApply.isEmpty()) {
            Set<PotionEffectType> types = new HashSet<>();
            for (Map.Entry<PotionEffectType, Integer> entry : effectsToApply.entrySet()) {
                PotionEffect effect = new PotionEffect(entry.getKey(), Integer.MAX_VALUE, entry.getValue(), false,
                        false, true);
                player.addPotionEffect(effect);
                types.add(entry.getKey());
            }
            appliedEffects.put(player.getUniqueId(), types);
        }
    }

    private void removeCurrentEffects(Player player) {
        Set<PotionEffectType> types = appliedEffects.remove(player.getUniqueId());
        if (types != null) {
            for (PotionEffectType type : types) {
                player.removePotionEffect(type);
            }
        }
    }
}
