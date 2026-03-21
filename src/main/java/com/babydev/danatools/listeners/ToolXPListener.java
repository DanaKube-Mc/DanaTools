package com.babydev.danatools.listeners;

import com.babydev.danatools.DanaTools;
import com.babydev.danatools.menus.ToolsMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ToolXPListener implements Listener {

    private final DanaTools plugin;

    public ToolXPListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        ItemStack handItem = killer.getInventory().getItemInMainHand();
        String mobType = event.getEntityType().name();
        handleMobKillXP(killer, handItem, mobType);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        String blockType = event.getBlock().getType().name();

        handleBlockBreakXP(player, handItem, blockType);
    }

    @EventHandler
    public void onToolInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        Material type = event.getClickedBlock().getType();
        String typeName = type.name();

        // --- Logique HOE (Houe) : Labourage ---
        boolean isTillable = (type == Material.GRASS_BLOCK || type == Material.DIRT ||
                type == Material.ROOTED_DIRT || type == Material.DIRT_PATH);
        if (isTillable && handItem.getType().name().contains("_HOE")) {
            handleActionXP(player, handItem, "on_till");
            return;
        }

        // --- Logique AXE (Hache) : Écorçage ---
        boolean isLog = (typeName.endsWith("_LOG") || typeName.endsWith("_WOOD") ||
                typeName.endsWith("_STEM") || typeName.endsWith("_HYPHAE"));

        if (isLog && !typeName.startsWith("STRIPPED_") && handItem.getType().name().contains("_AXE")) {
            handleActionXP(player, handItem, "on_strip");
            return;
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() != Material.FISHING_ROD) {
            handItem = player.getInventory().getItemInOffHand();
        }

        String caughtType = "UNKNOWN";
        if (event.getCaught() != null) {
            if (event.getCaught() instanceof org.bukkit.entity.Item itemEntity) {
                caughtType = itemEntity.getItemStack().getType().name();
            } else {
                caughtType = event.getCaught().getType().name();
            }
        }

        handleFishXP(player, handItem, caughtType);
    }

    private void handleFishXP(Player player, ItemStack item, String caughtType) {
        if (item == null || item.getType() == Material.AIR)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        String toolId = meta.getPersistentDataContainer().get(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING);
        if (toolId == null)
            return;

        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        int amount = getXPValue(player, toolId, toolConfig, "xp.catch." + caughtType, -1);
        if (amount == -1) {
            amount = getXPValue(player, toolId, toolConfig, "xp.on_catch", 0);
        }

        if (amount <= 0)
            return;

        int finalAmount = calculateBonusXP(player, toolId, toolConfig, amount);
        processXPGain(player, toolId, toolConfig, finalAmount);
    }

    private void handleBlockBreakXP(Player player, ItemStack item, String blockType) {
        if (item == null || item.getType() == Material.AIR)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        String toolId = meta.getPersistentDataContainer().get(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING);
        if (toolId == null)
            return;

        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        int amount = getXPValue(player, toolId, toolConfig, "xp.blocks." + blockType, -1);
        if (amount == -1) {
            amount = getXPValue(player, toolId, toolConfig, "xp.on_block_break", 0);
        }

        if (amount <= 0)
            return;

        int finalAmount = calculateBonusXP(player, toolId, toolConfig, amount);
        processXPGain(player, toolId, toolConfig, finalAmount);
    }

    private void handleActionXP(Player player, ItemStack item, String actionKey) {
        if (item == null || item.getType() == Material.AIR)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        String toolId = meta.getPersistentDataContainer().get(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING);
        if (toolId == null)
            return;

        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        int amount = getXPValue(player, toolId, toolConfig, "xp." + actionKey, 0);
        if (amount <= 0)
            return;

        int finalAmount = calculateBonusXP(player, toolId, toolConfig, amount);
        processXPGain(player, toolId, toolConfig, finalAmount);
    }

    private void handleMobKillXP(Player player, ItemStack item, String mobType) {
        if (item == null || item.getType() == Material.AIR)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        String toolId = meta.getPersistentDataContainer().get(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING);
        if (toolId == null)
            return;

        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        int amount = getXPValue(player, toolId, toolConfig, "xp.mobs." + mobType, -1);
        if (amount == -1) {
            amount = getXPValue(player, toolId, toolConfig, "xp.on_kill", 0);
        }

        if (amount <= 0)
            return;

        int finalAmount = calculateBonusXP(player, toolId, toolConfig, amount);
        processXPGain(player, toolId, toolConfig, finalAmount);
    }

    private int getXPValue(Player player, String toolId, FileConfiguration toolConfig, String path, int defaultValue) {
        if (!toolConfig.contains(path))
            return defaultValue;

        if (toolConfig.isConfigurationSection(path)) {
            ConfigurationSection section = toolConfig.getConfigurationSection(path);
            if (section == null)
                return 0;

            if (section.contains("requirements")) {
                ConfigurationSection reqs = section.getConfigurationSection("requirements");
                if (reqs != null) {
                    if (reqs.contains("level")) {
                        int requiredLevel = reqs.getInt("level");
                        int currentLevel = plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId);
                        if (currentLevel < requiredLevel)
                            return 0;
                    }
                    if (reqs.contains("permission")) {
                        String perm = reqs.getString("permission");
                        if (perm != null && !player.hasPermission(perm))
                            return 0;
                    }
                }
            }
            return section.getInt("xp", 0);
        }
        return toolConfig.getInt(path, defaultValue);
    }

    public double getActiveBonusPercentage(Player player, String toolId, FileConfiguration toolConfig) {
        ConfigurationSection bonusSection = toolConfig.getConfigurationSection("xp.bonus");
        if (bonusSection == null)
            return 0;

        int highestWeight = -1;
        double bestPercentage = 0;

        for (String key : bonusSection.getKeys(false)) {
            ConfigurationSection bonus = bonusSection.getConfigurationSection(key);
            if (bonus == null)
                continue;

            int weight = bonus.getInt("weight", 0);
            double percentage = bonus.getDouble("porcentage", 0);

            boolean meetsRequirements = true;
            if (bonus.contains("requirements")) {
                ConfigurationSection reqs = bonus.getConfigurationSection("requirements");
                if (reqs != null) {
                    if (reqs.contains("level")) {
                        int requiredLevel = reqs.getInt("level");
                        int currentLevel = plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId);
                        if (currentLevel < requiredLevel)
                            meetsRequirements = false;
                    }
                    if (meetsRequirements && reqs.contains("permission")) {
                        String perm = reqs.getString("permission");
                        if (perm != null && !player.hasPermission(perm))
                            meetsRequirements = false;
                    }
                }
            }

            if (meetsRequirements) {
                if (weight > highestWeight) {
                    highestWeight = weight;
                    bestPercentage = percentage;
                }
            }
        }
        return bestPercentage;
    }

    private int calculateBonusXP(Player player, String toolId, FileConfiguration toolConfig, int baseXP) {
        double bestPercentage = getActiveBonusPercentage(player, toolId, toolConfig);

        if (bestPercentage == 0)
            return baseXP;

        double bonusXP = baseXP * (bestPercentage / 100.0);
        int finalBonus;
        if (bonusXP >= 0) {
            finalBonus = (int) Math.ceil(bonusXP);
        } else {
            finalBonus = (int) Math.floor(bonusXP);
        }

        return baseXP + finalBonus;
    }

    private ConfigurationSection getEffectiveLevelSection(FileConfiguration toolConfig, int level) {
        ConfigurationSection levels = toolConfig.getConfigurationSection("levels");
        if (levels == null)
            return null;

        for (int i = level; i >= 0; i--) {
            ConfigurationSection section = levels.getConfigurationSection(String.valueOf(i));
            if (section != null)
                return section;
        }
        return null;
    }

    public int getNextLevelXP(FileConfiguration toolConfig, int currentLevel) {
        if (toolConfig.getBoolean("xp.use_equation_xp", true)) {
            int reqBase = toolConfig.getInt("xp.required_base", 100);
            int reqMult = toolConfig.getInt("xp.required_multiplier", 50);
            return reqBase + (currentLevel * reqMult);
        } else {
            ConfigurationSection effectiveSection = getEffectiveLevelSection(toolConfig, currentLevel);
            return (effectiveSection != null) ? effectiveSection.getInt("xp_required", 100) : 100;
        }
    }

    public void processXPGain(Player player, String toolId, FileConfiguration toolConfig, int amount) {
        if (amount <= 0)
            return;

        int currentLevel = plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId);
        int currentXP = plugin.getPlayerStatsManager().getToolXP(player.getUniqueId(), toolId);
        int maxLevel = toolConfig.getInt("max_level", 100);

        if (currentLevel >= maxLevel) {
            if (currentXP != 0) {
                plugin.getPlayerStatsManager().updateToolStats(player.getUniqueId(), toolId, currentLevel, 0);
            }
            return;
        }

        int newXP = currentXP + amount;

        while (currentLevel < maxLevel) {
            int nextLevelXP = getNextLevelXP(toolConfig, currentLevel);

            if (newXP >= nextLevelXP) {
                ConfigurationSection nextLevelSection = toolConfig
                        .getConfigurationSection("levels." + (currentLevel + 1));
                if (nextLevelSection != null && nextLevelSection.contains("permissions")) {
                    List<String> perms = nextLevelSection.getStringList("permissions");
                    boolean hasAll = true;
                    for (String perm : perms) {
                        if (!player.hasPermission(perm)) {
                            hasAll = false;
                            break;
                        }
                    }
                    if (!hasAll)
                        break;
                }

                newXP -= nextLevelXP;

                // --- RÉCOMPENSES ---
                applyRewards(player, toolId, toolConfig, currentLevel);

                currentLevel++;

                // --- Message et Son ---
                String message = plugin.getMessage("tool_level_up").replace("%level%", String.valueOf(currentLevel));
                player.sendMessage(message);

                String soundStr = toolConfig.getString("visual.level_up_sound", "ENTITY_PLAYER_LEVELUP");
                try {
                    player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(soundStr), 1.0f, 1.0f);
                } catch (Exception ignored) {
                }

                if (currentLevel >= maxLevel) {
                    newXP = 0;
                    break;
                }
            } else {
                break;
            }
        }

        if (currentLevel >= maxLevel) {
            newXP = 0;
        }

        plugin.getPlayerStatsManager().updateToolStats(player.getUniqueId(), toolId, currentLevel, newXP);

        ToolsMenu toolsMenu = new ToolsMenu(plugin);
        ItemStack newItem = toolsMenu.createToolItemStack(player, toolId, toolConfig);
        player.getInventory().setItemInMainHand(newItem);
        plugin.getToolEffectListener().updateEffects(player, newItem);

        // --- Message Action Bar --- 
        if (plugin.getConfig().getBoolean("messages_action_barre", true)) {
            int nextXP = getNextLevelXP(toolConfig, currentLevel);
            String rawMessage = plugin.getRawMessage("xp_gain_action_bar")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%current%", String.valueOf(newXP))
                    .replace("%max%", String.valueOf(nextXP));

            Component actionBar;
            if (rawMessage.contains("<") && rawMessage.contains(">")) {
                actionBar = MiniMessage.miniMessage().deserialize(rawMessage);
            } else {
                actionBar = LegacyComponentSerializer.legacySection().deserialize(rawMessage);
            }
            player.sendActionBar(actionBar);
        }
    }

    private void applyRewards(Player player, String toolId, FileConfiguration toolConfig, int level) {
        ConfigurationSection levelSection = toolConfig.getConfigurationSection("levels." + level);
        if (levelSection == null)
            return;

        ConfigurationSection rewardsSection = levelSection.getConfigurationSection("rewards");
        if (rewardsSection == null)
            return;

        // 1. Messages
        List<String> messages = rewardsSection.getStringList("messages");
        for (String msg : messages) {
            player.sendMessage(msg.replace("&", "§"));
        }

        // 2. Commandes
        List<String> commands = rewardsSection.getStringList("commands");
        for (String cmd : commands) {
            String finalCmd = cmd.replace("%player%", player.getName());
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), finalCmd);
        }

        // 3. Réparation (Optionnel)
        if (rewardsSection.getBoolean("repair", false)) {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                damageable.setDamage(0);
                item.setItemMeta(meta);
            }
        }

        // 4. Points de Skill (Optionnel)
        if (rewardsSection.contains("skills_points")) {
            int pointsToAdd = rewardsSection.getInt("skills_points", 0);
            if (pointsToAdd > 0) {
                int currentPoints = plugin.getPlayerStatsManager().getSkillPoints(player.getUniqueId(), toolId);
                plugin.getPlayerStatsManager().setSkillPoints(player.getUniqueId(), toolId,
                        currentPoints + pointsToAdd);
            }
        }

        // 5. Argent (Vault)
        if (rewardsSection.contains("money") && plugin.getConfig().getBoolean("settings.use_vault", false) && plugin.getEconomy() != null) {
            double moneyToAdd = rewardsSection.getDouble("money", 0);
            if (moneyToAdd > 0) {
                plugin.getEconomy().depositPlayer(player, moneyToAdd);
            }
        }
    }
}
