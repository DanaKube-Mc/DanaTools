package com.babydev.danatools.listeners;

import com.babydev.danatools.DanaTools;
import com.babydev.danatools.menus.SkillMenu;
import com.babydev.danatools.menus.SkillMenuHolder;
import com.babydev.danatools.menus.ToolDetailMenu;
import com.babydev.danatools.menus.ToolDetailMenuHolder;
import com.babydev.danatools.menus.ToolsMenu;
import com.babydev.danatools.menus.ToolsMenuHolder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MenuListener implements Listener {

    private final DanaTools plugin;

    public MenuListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ToolsMenuHolder) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null)
                return;

            NamespacedKey toolIdKey = DanaTools.TOOL_ID_KEY;
            String toolId = meta.getPersistentDataContainer().get(toolIdKey, PersistentDataType.STRING);

            if (toolId != null) {
                // --- CLIC DROIT : Ouvrir les détails ---
                if (event.getClick().isRightClick()) {
                    ToolDetailMenu detailMenu = new ToolDetailMenu(plugin);
                    detailMenu.open(player, toolId);
                    return;
                }

                // --- CLIC GAUCHE : Donner / Ranger l'outil ---
                boolean alreadyHasIt = false;
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (item == null || item.getItemMeta() == null)
                        continue;

                    String itemId = item.getItemMeta().getPersistentDataContainer().get(toolIdKey,
                            PersistentDataType.STRING);
                    if (toolId.equals(itemId)) {
                        player.getInventory().setItem(i, null);
                        alreadyHasIt = true;
                    }
                }

                if (alreadyHasIt) {
                    player.sendMessage(plugin.getMessage("tool_stored"));
                } else {
                    // --- Vérification permission Niveau 0 (Débloquage) ---
                    org.bukkit.configuration.file.FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
                    if (toolConfig != null) {
                        if (!plugin.getPlayerStatsManager().isToolUnlocked(player.getUniqueId(), toolId)) {
                            org.bukkit.configuration.ConfigurationSection level0 = toolConfig
                                    .getConfigurationSection("levels.0");
                            if (level0 != null && level0.contains("permissions")) {
                                java.util.List<String> perms = level0.getStringList("permissions");
                                for (String perm : perms) {
                                    if (!player.hasPermission(perm)) {
                                        player.sendMessage(plugin.getMessage("no_permission_unlock_tool"));
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    ItemStack toolToGive = clickedItem.clone();
                    player.getInventory().addItem(toolToGive);
                    player.sendMessage(plugin.getMessage("tool_received"));
                }
                player.closeInventory();
            }
        } else if (event.getInventory().getHolder() instanceof ToolDetailMenuHolder) {
            event.setCancelled(true);
            ToolDetailMenuHolder holder = (ToolDetailMenuHolder) event.getInventory().getHolder();
            String toolId = holder.getToolId();

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null)
                return;

            if (clickedItem.getType() == Material.ARROW && event.getSlot() == 0) {
                new ToolsMenu(plugin).open((Player) event.getWhoClicked());
                return;
            }

            if (clickedItem.getType() == Material.ENCHANTED_BOOK && event.getSlot() == 15) {
                new SkillMenu(plugin).open((Player) event.getWhoClicked(), toolId);
                return;
            }

        } else if (event.getInventory().getHolder() instanceof SkillMenuHolder) {
            event.setCancelled(true);
            SkillMenuHolder holder = (SkillMenuHolder) event.getInventory().getHolder();
            String toolId = holder.getToolId();
            Player player = (Player) event.getWhoClicked();

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null)
                return;

            if (clickedItem.getType() == Material.ARROW && event.getSlot() == 0) {
                new ToolDetailMenu(plugin).open(player, toolId);
                return;
            }

            // --- Logique d'upgrade d'un Skill ---
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null
                    && meta.getPersistentDataContainer().has(SkillMenu.SKILL_ID_KEY, PersistentDataType.STRING)) {
                String skillId = meta.getPersistentDataContainer().get(SkillMenu.SKILL_ID_KEY,
                        PersistentDataType.STRING);
                FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
                if (toolConfig == null)
                    return;

                ConfigurationSection skill = toolConfig.getConfigurationSection("skills." + skillId);
                if (skill == null)
                    return;

                int currentLvl = plugin.getPlayerStatsManager().getSkillLevel(player.getUniqueId(), toolId, skillId);
                int maxLvl = skill.getInt("max_upgrade", 1);

                if (currentLvl >= maxLvl) {
                    player.sendMessage(plugin.getMessage("skill_max_level"));
                    return;
                }

                // --- Vérification des coûts ---
                SkillMenu skillMenu = new SkillMenu(plugin);
                int nextLvl = currentLvl + 1;
                boolean additional = skill.getBoolean("use_additional_increase", true);
                double increase = skill.getDouble("prise.increase", 0);

                long costExp = skillMenu.calculateCost(skill.getInt("prise.expererince", 0), nextLvl, additional,
                        increase);
                long costPts = skillMenu.calculateCost(skill.getInt("prise.skills_points", 0), nextLvl, additional,
                        increase);
                long costMoney = skillMenu.calculateCost(skill.getInt("prise.money", 0), nextLvl, additional, increase);

                // Check prérequis techniques
                int reqLvl = skill.getInt("requirements.level", 0);
                if (plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId) < reqLvl) {
                    player.sendMessage(plugin.getMessage("skill_requires_tool_level"));
                    return;
                }
                String reqPerm = skill.getString("requirements.permission");
                if (reqPerm != null && !player.hasPermission(reqPerm)) {
                    player.sendMessage(plugin.getMessage("no_permission_unlock_skill"));
                    return;
                }

                // Check ressources
                if (player.getLevel() < costExp) {
                    player.sendMessage(plugin.getMessage("not_enough_xp_levels"));
                    return;
                }
                if (plugin.getPlayerStatsManager().getSkillPoints(player.getUniqueId(), toolId) < costPts) {
                    player.sendMessage(plugin.getMessage("not_enough_skill_points"));
                    return;
                }
                if (costMoney > 0 && plugin.getConfig().getBoolean("settings.use_vault", false) && plugin.getEconomy() != null) {
                    if (plugin.getEconomy().getBalance(player) < costMoney) {
                        player.sendMessage(plugin.getMessage("not_enough_money"));
                        return;
                    }
                }

                // --- Payer les coûts ---
                player.setLevel(player.getLevel() - (int) costExp);
                int currentPts = plugin.getPlayerStatsManager().getSkillPoints(player.getUniqueId(), toolId);
                plugin.getPlayerStatsManager().setSkillPoints(player.getUniqueId(), toolId, currentPts - (int) costPts);
                
                if (costMoney > 0 && plugin.getConfig().getBoolean("settings.use_vault", false) && plugin.getEconomy() != null) {
                    plugin.getEconomy().withdrawPlayer(player, costMoney);
                }
                
                // --- Upgrade ---
                plugin.getPlayerStatsManager().setSkillLevel(player.getUniqueId(), toolId, skillId, nextLvl);
                player.sendMessage(plugin.getMessage("skill_upgraded").replace("%level%", String.valueOf(nextLvl)));
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                skillMenu.open(player, toolId);
            }
        }
    }
}
