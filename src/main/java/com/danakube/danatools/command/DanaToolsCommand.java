package com.danakube.danatools.command;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.storage.ToolDataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DanaToolsCommand implements CommandExecutor, TabCompleter {

    private final DanaTools plugin;

    public DanaToolsCommand(DanaTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("danatools.admin")) {
            sender.sendMessage(plugin.getLangManager().getMessage("no_permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage(plugin.getLangManager().getMessage("reload_success"));
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "givemodifier":
                handleGiveModifier(sender, args);
                break;
            case "addxp":
                handleXP(sender, args);
                break;
            case "setlevel":
                handleLevel(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLangManager().getMessage("help.header"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.give"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.givemodifier"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.addxp"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.setlevel"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.reload"));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.give"));
            return;
        }

        String toolId = args[1];
        CustomTool toolConfig = plugin.getToolConfigManager().getTool(toolId);
        if (toolConfig == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("give.tool_not_found", "{tool}", toolId));
            return;
        }

        Player target = null;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.getLangManager().getMessage("player_not_found", "{player}", args[2]));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(plugin.getLangManager().getMessage("specify_player"));
            return;
        }

        ItemStack item = new ItemStack(toolConfig.getMaterial());
        int initialSlots = toolConfig.getSlotsForLevel(1);
        ToolDataStorage.initToolData(item, toolId, initialSlots);

        DanaItemInstance toolInstance = DanaItemInstance.fromItemStack(item);
        if (toolInstance != null) {
            toolInstance.updateLore();
        }

        target.getInventory().addItem(item);
        sender.sendMessage(plugin.getLangManager().getMessage("give.success", "{tool}", toolId, "{player}", target.getName()));
    }

    private void handleGiveModifier(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.givemodifier"));
            return;
        }

        String modId = args[1];
        CustomModifier modifier = plugin.getModifierConfigManager().getModifier(modId);
        if (modifier == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("givemodifier.modifier_not_found", "{modifier}", modId));
            return;
        }

        Player target = null;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.getLangManager().getMessage("player_not_found", "{player}", args[2]));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(plugin.getLangManager().getMessage("specify_player"));
            return;
        }

        ItemStack template = new ItemStack(modifier.getTemplateMaterial());
        ItemMeta tMeta = template.getItemMeta();
        if (tMeta != null) {
            tMeta.displayName(DanaItemInstance.parseColor(modifier.getTemplateDisplayName()));
            if (modifier.getTemplateCustomModelData() > 0) {
                tMeta.setCustomModelData(modifier.getTemplateCustomModelData());
            }
            List<Component> tLore = new ArrayList<>();
            CustomModifier.LevelSettings lvl1 = modifier.getLevel(1);
            if (lvl1 != null) {
                for (String l : lvl1.getLore()) {
                    tLore.add(DanaItemInstance.parseColor(l));
                }
            }
            tMeta.lore(tLore);
            template.setItemMeta(tMeta);
        }

        ItemStack ingredient = plugin.getModifierConfigManager().buildIngredientItem(modifier);

        target.getInventory().addItem(template, ingredient);
        sender.sendMessage(plugin.getLangManager().getMessage("givemodifier.success", "{modifier}", modId, "{player}", target.getName()));
    }

    private void handleXP(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.addxp"));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLangManager().getMessage("invalid_amount"));
            return;
        }

        Player target = null;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.getLangManager().getMessage("player_not_found", "{player}", args[2]));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(plugin.getLangManager().getMessage("specify_player"));
            return;
        }

        ItemStack handItem = target.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(handItem);
        if (tool == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("xp.no_tool", "{player}", target.getName()));
            return;
        }

        tool.addXP(amount, target);
        sender.sendMessage(plugin.getLangManager().getMessage("xp.success", "{amount}", amount, "{player}", target.getName()));
    }

    private void handleLevel(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.setlevel"));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLangManager().getMessage("invalid_amount"));
            return;
        }

        Player target = null;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.getLangManager().getMessage("player_not_found", "{player}", args[2]));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(plugin.getLangManager().getMessage("specify_player"));
            return;
        }

        ItemStack handItem = target.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(handItem);
        if (tool == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("xp.no_tool", "{player}", target.getName()));
            return;
        }

        int maxLvl = tool.getConfig().getMaxLevel();
        if (level < 1 || level > maxLvl) {
            sender.sendMessage(plugin.getLangManager().getMessage("level.invalid_range", "{max_level}", maxLvl));
            return;
        }

        ToolDataStorage.setLevel(handItem, level);
        ToolDataStorage.setXp(handItem, 0);

        int slots = tool.getConfig().getSlotsForLevel(level);
        ToolDataStorage.setSlotsTotal(handItem, slots);

        tool.updateLore();
        sender.sendMessage(plugin.getLangManager().getMessage("level.success", "{level}", level, "{player}", target.getName()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("danatools.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filterList(Arrays.asList("reload", "give", "givemodifier", "addxp", "setlevel"), args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give")) {
                return filterList(new ArrayList<>(plugin.getToolConfigManager().getTools().stream().map(CustomTool::getId).collect(Collectors.toList())), args[1]);
            }
            if (sub.equals("givemodifier")) {
                return filterList(new ArrayList<>(plugin.getModifierConfigManager().getModifiers().stream().map(CustomModifier::getId).collect(Collectors.toList())), args[1]);
            }
            if (sub.equals("addxp") || sub.equals("setlevel")) {
                return Arrays.asList("1", "5", "10");
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("givemodifier") || sub.equals("addxp") || sub.equals("setlevel")) {
                return filterList(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterList(List<String> list, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lowerPrefix)).collect(Collectors.toList());
    }
}
