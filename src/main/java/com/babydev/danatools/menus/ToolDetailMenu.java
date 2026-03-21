package com.babydev.danatools.menus;

import com.babydev.danatools.DanaTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ToolDetailMenu {

    private final DanaTools plugin;

    public ToolDetailMenu(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String toolId) {
        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        String titleStr = "<dark_aqua>Détails: " + toolConfig.getString("name", toolId);
        Inventory inventory = Bukkit.createInventory(new ToolDetailMenuHolder(toolId), 54,
                MiniMessage.miniMessage().deserialize(titleStr));
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(MiniMessage.miniMessage().deserialize("<red><bold>« RETOUR"));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(0, backButton);

        int slot = 1;

        ConfigurationSection xpSection = toolConfig.getConfigurationSection("xp");
        if (xpSection != null) {
            String[] actions = { "on_kill", "on_block_break", "on_catch", "on_till", "on_strip" };
            Material[] mats = { Material.IRON_SWORD, Material.IRON_PICKAXE, Material.FISHING_ROD, Material.IRON_HOE,
                    Material.IRON_AXE };
            String[] names = { "Kill", "Minage", "Pêche", "Labourage", "Écorçage" };

            for (int i = 0; i < actions.length; i++) {
                int xpValue = xpSection.getInt(actions[i], 0);
                if (xpValue > 0) {
                    if (slot < 9) {
                        ItemStack item = new ItemStack(mats[i]);
                        ItemMeta m = item.getItemMeta();
                        if (m != null) {
                            m.displayName(MiniMessage.miniMessage()
                                    .deserialize("<yellow>" + names[i] + " <gray>: <gold>+" + xpValue + " XP"));
                            item.setItemMeta(m);
                        }
                        inventory.setItem(slot++, item);
                    }
                }
            }
            slot = 18;
            addXpSourceIcons(inventory, xpSection.getConfigurationSection("mobs"), Material.ZOMBIE_HEAD, slot);
            slot = 27;
            addXpSourceIcons(inventory, xpSection.getConfigurationSection("blocks"), Material.DIAMOND_ORE, slot);
        }


        ToolsMenu toolsMenu = new ToolsMenu(plugin);
        ItemStack toolIcon = toolsMenu.createToolItemStack(player, toolId, toolConfig);
        inventory.setItem(13, toolIcon);


        if (toolConfig.getBoolean("use_skills", false)) {
            ItemStack skillButton = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta skillMeta = skillButton.getItemMeta();
            if (skillMeta != null) {
                skillMeta.displayName(MiniMessage.miniMessage().deserialize("<gold><bold>⚡ COMPÉTENCES (SKILLS)"));
                List<Component> lore = new ArrayList<>();
                int pts = plugin.getPlayerStatsManager().getSkillPoints(player.getUniqueId(), toolId);
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Points disponibles : <yellow>" + pts));
                lore.add(Component.empty());
                lore.add(MiniMessage.miniMessage().deserialize("<yellow>▶ Cliquez pour améliorer votre outil !"));
                skillMeta.lore(lore);
                skillButton.setItemMeta(skillMeta);
            }
            inventory.setItem(15, skillButton);
        }

        player.openInventory(inventory);
    }

    private void addXpSourceIcons(Inventory inv, ConfigurationSection section, Material iconType, int startSlot) {
        if (section == null)
            return;
        int slot = startSlot;
        for (String key : section.getKeys(false)) {
            if (slot >= startSlot + 9 || slot >= inv.getSize())
                break;

            ItemStack item = new ItemStack(iconType);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int xp;
                if (section.isConfigurationSection(key)) {
                    xp = section.getInt(key + ".xp", 0);
                } else {
                    xp = section.getInt(key, 0);
                }
                meta.displayName(
                        MiniMessage.miniMessage().deserialize("<yellow>" + key + " <gray>: <gold>+" + xp + " XP"));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
    }
}
