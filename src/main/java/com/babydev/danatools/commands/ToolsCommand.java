package com.babydev.danatools.commands;

import com.babydev.danatools.DanaTools;
import com.babydev.danatools.menus.ToolsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe gérant l'exécution de la commande /tools.
 */
public class ToolsCommand implements TabExecutor {

    private final DanaTools plugin;

    public ToolsCommand(DanaTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String reloadPerm = plugin.getPermissionsConfig().getString("reload", "danatools.reload");
        String helpPerm = plugin.getPermissionsConfig().getString("help", "danatools.help");
        String useToolsPerm = plugin.getPermissionsConfig().getString("use_tools", "");
        String adminPerm = plugin.getPermissionsConfig().getString("admin", "danatools.admin");

        // Sous-commande : /tools reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!(sender instanceof Player) || sender.isOp() || reloadPerm.isEmpty()
                    || sender.hasPermission(reloadPerm)) {
                plugin.reloadConfig();
                plugin.reloadPermissionsConfig();
                plugin.reloadLangConfig();
                plugin.loadToolsConfigs();
                plugin.getPlayerStatsManager().saveAndClearCache();
                sender.sendMessage(plugin.getMessage("reload_success"));
            } else {
                sender.sendMessage(plugin.getMessage("no_permission"));
            }
            return true;
        }

        // Sous-commande : /tools help
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            if (!(sender instanceof Player) || sender.isOp() || helpPerm.isEmpty() || sender.hasPermission(helpPerm)) {
                sender.sendMessage(plugin.getMessage("help_header"));
                ConfigurationSection helpSection = plugin.getLangConfig().getConfigurationSection("help");
                if (helpSection != null) {
                    for (String key : helpSection.getKeys(false)) {
                        sender.sendMessage(plugin.getMessage("help." + key));
                    }
                }
            } else {
                sender.sendMessage(plugin.getMessage("no_permission"));
            }
            return true;
        }

        // --- COMMANDES ADMIN ---
        if (args.length >= 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("addxp")
                || args[0].equalsIgnoreCase("removexp") || args[0].equalsIgnoreCase("set"))) {
            if (!sender.isOp() && !adminPerm.isEmpty() && !sender.hasPermission(adminPerm)) {
                sender.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("player_not_found"));
                return true;
            }

            String toolId = args[2];
            org.bukkit.configuration.file.FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
            if (toolConfig == null) {
                sender.sendMessage(plugin.getMessage("tool_not_found").replace("%id%", toolId));
                return true;
            }

            // --- GIVE ---
            if (args[0].equalsIgnoreCase("give")) {
                int level = 0;
                if (args.length >= 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                        if (level < 0) {
                            sender.sendMessage(plugin.getMessage("invalid_amount"));
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getMessage("invalid_amount"));
                        return true;
                    }
                }
                plugin.getPlayerStatsManager().updateToolStats(target.getUniqueId(), toolId, level, 0);

                ToolsMenu toolsMenu = new ToolsMenu(plugin);
                target.getInventory().addItem(toolsMenu.createToolItemStack(target, toolId, toolConfig));
                sender.sendMessage(plugin.getMessage("tool_given_admin").replace("%id%", toolId)
                        .replace("%level%", String.valueOf(level)).replace("%player%", target.getName()));
                return true;
            }

            // --- ADD XP ---
            if (args[0].equalsIgnoreCase("addxp")) {
                if (args.length < 4)
                    return false;
                try {
                    int amount = Integer.parseInt(args[3]);
                    if (amount < 0) {
                        sender.sendMessage(plugin.getMessage("invalid_amount"));
                        return true;
                    }

                    plugin.getToolXPListener().processXPGain(target, toolId, toolConfig, amount);

                    sender.sendMessage(plugin.getMessage("xp_added").replace("%amount%", String.valueOf(amount))
                            .replace("%id%", toolId).replace("%player%", target.getName()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("invalid_amount"));
                }
                return true;
            }

            // --- REMOVE XP ---
            if (args[0].equalsIgnoreCase("removexp")) {
                if (args.length < 4)
                    return false;
                try {
                    int amount = Integer.parseInt(args[3]);
                    if (amount < 0) {
                        sender.sendMessage(plugin.getMessage("invalid_amount"));
                        return true;
                    }

                    int currentLevel = plugin.getPlayerStatsManager().getToolLevel(target.getUniqueId(), toolId);
                    int currentXP = plugin.getPlayerStatsManager().getToolXP(target.getUniqueId(), toolId);
                    int newXP = Math.max(0, currentXP - amount);
                    plugin.getPlayerStatsManager().updateToolStats(target.getUniqueId(), toolId, currentLevel, newXP);
                    refreshTargetTool(target, toolId, toolConfig);
                    sender.sendMessage(plugin.getMessage("xp_removed").replace("%amount%", String.valueOf(amount))
                            .replace("%id%", toolId).replace("%player%", target.getName()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("invalid_amount"));
                }
                return true;
            }

            // --- SET ---
            if (args[0].equalsIgnoreCase("set")) {
                if (args.length < 5)
                    return false;
                String type = args[3].toLowerCase();
                try {
                    int val = Integer.parseInt(args[4]);
                    if (val < 0) {
                        sender.sendMessage(plugin.getMessage("invalid_amount"));
                        return true;
                    }

                    int level = plugin.getPlayerStatsManager().getToolLevel(target.getUniqueId(), toolId);
                    int xp = plugin.getPlayerStatsManager().getToolXP(target.getUniqueId(), toolId);

                    if (type.equals("level")) {
                        level = val;
                        xp = 0;
                        sender.sendMessage(plugin.getMessage("level_set").replace("%value%", String.valueOf(val))
                                .replace("%id%", toolId).replace("%player%", target.getName()));
                    } else if (type.equals("xp")) {
                        int nextLevelXP = plugin.getToolXPListener().getNextLevelXP(toolConfig, level);
                        xp = Math.min(val, nextLevelXP);
                        sender.sendMessage(plugin.getMessage("xp_set").replace("%value%", String.valueOf(xp))
                                .replace("%id%", toolId).replace("%player%", target.getName()));
                    } else {
                        return false;
                    }
                    plugin.getPlayerStatsManager().updateToolStats(target.getUniqueId(), toolId, level, xp);
                    refreshTargetTool(target, toolId, toolConfig);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("invalid_amount"));
                }
                return true;
            }
        }

        // --- LOOK ---
        if (args.length >= 2 && args[0].equalsIgnoreCase("look")) {
            if (!sender.isOp() && !adminPerm.isEmpty() && !sender.hasPermission(adminPerm)) {
                sender.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("console_only_reload"));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("player_not_found"));
                return true;
            }

            Player admin = (Player) sender;
            ToolsMenu toolsMenu = new ToolsMenu(plugin);
            toolsMenu.open(admin, target);
            return true;
        }

        // Commande : /tools (Menu)
        if (!sender.isOp() && !useToolsPerm.isEmpty() && !sender.hasPermission(useToolsPerm)) {
            sender.sendMessage(plugin.getMessage("no_permission_menu"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("console_only_reload"));
            return true;
        }

        Player player = (Player) sender;
        ToolsMenu toolsMenu = new ToolsMenu(plugin);
        toolsMenu.open(player);

        return true;
    }

    private void refreshTargetTool(Player target, String toolId,
            org.bukkit.configuration.file.FileConfiguration toolConfig) {
        org.bukkit.inventory.ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType() == org.bukkit.Material.AIR)
            return;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        String id = meta.getPersistentDataContainer().get(DanaTools.TOOL_ID_KEY,
                org.bukkit.persistence.PersistentDataType.STRING);
        if (toolId.equals(id)) {
            ToolsMenu toolsMenu = new ToolsMenu(plugin);
            target.getInventory().setItemInMainHand(toolsMenu.createToolItemStack(target, toolId, toolConfig));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        String reloadPerm = plugin.getPermissionsConfig().getString("reload", "danatools.reload");
        String helpPerm = plugin.getPermissionsConfig().getString("help", "danatools.help");
        String adminPerm = plugin.getPermissionsConfig().getString("admin", "danatools.admin");

        if (args.length == 1) {
            String[] subs = { "reload", "help", "give", "addxp", "removexp", "set", "look" };
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    if (s.equals("reload") && !sender.hasPermission(reloadPerm))
                        continue;
                    if (s.equals("help") && !sender.hasPermission(helpPerm))
                        continue;
                    if ((s.equals("give") || s.equals("addxp") || s.equals("removexp") || s.equals("set")
                            || s.equals("look"))
                            && !sender.hasPermission(adminPerm))
                        continue;
                    completions.add(s);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("addxp")
                || args[0].equalsIgnoreCase("removexp") || args[0].equalsIgnoreCase("set")
                || args[0].equalsIgnoreCase("look"))) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("addxp")
                || args[0].equalsIgnoreCase("removexp") || args[0].equalsIgnoreCase("set"))) {
            for (String tid : plugin.getToolsConfigs().keySet()) {
                if (tid.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(tid);
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            if ("level".startsWith(args[3].toLowerCase()))
                completions.add("level");
            if ("xp".startsWith(args[3].toLowerCase()))
                completions.add("xp");
        }

        return completions;
    }
}
