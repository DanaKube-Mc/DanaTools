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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class SkillMenu {

    private final DanaTools plugin;
    public static final NamespacedKey SKILL_ID_KEY = new NamespacedKey("danatools", "skill_id");

    public SkillMenu(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String toolId) {
        FileConfiguration toolConfig = plugin.getToolsConfigs().get(toolId);
        if (toolConfig == null)
            return;

        int skillPoints = plugin.getPlayerStatsManager().getSkillPoints(player.getUniqueId(), toolId);
        String title = "<gold>Skills: <white>" + toolId + " <gray>(Pts: " + skillPoints + ")";

        Inventory inventory = Bukkit.createInventory(new SkillMenuHolder(toolId), 27,
                MiniMessage.miniMessage().deserialize(title));

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(MiniMessage.miniMessage().deserialize("<red><bold>« RETOUR"));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(0, backButton);

        ConfigurationSection skillsSection = toolConfig.getConfigurationSection("skills");
        if (skillsSection != null) {
            int slot = 10;
            for (String skillId : skillsSection.getKeys(false)) {
                ConfigurationSection skill = skillsSection.getConfigurationSection(skillId);
                if (skill == null)
                    continue;

                ItemStack item = createSkillItem(player, toolId, skillId, skill);
                inventory.setItem(slot++, item);
                if (slot == 17)
                    slot = 19;
            }
        }

        player.openInventory(inventory);
    }

    private ItemStack createSkillItem(Player player, String toolId, String skillId, ConfigurationSection skill) {
        Material mat = Material.matchMaterial(skill.getString("item", "ENCHANTED_BOOK"));
        if (mat == null)
            mat = Material.ENCHANTED_BOOK;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = skill.getString("name", skillId);
            int currentLvl = plugin.getPlayerStatsManager().getSkillLevel(player.getUniqueId(), toolId, skillId);
            int maxLvl = skill.getInt("max_upgrade", 1);

            meta.displayName(MiniMessage.miniMessage()
                    .deserialize("<yellow>" + name + " <gray>(Lvl " + currentLvl + "/" + maxLvl + ")"));

            List<Component> lore = new ArrayList<>();

            if (currentLvl < maxLvl) {
                int nextLvl = currentLvl + 1;
                long costExp = calculateCost(skill.getInt("prise.expererince", 0), nextLvl,
                        skill.getBoolean("use_additional_increase", true), skill.getDouble("prise.increase", 0));
                long costPts = calculateCost(skill.getInt("prise.skills_points", 0), nextLvl,
                        skill.getBoolean("use_additional_increase", true), skill.getDouble("prise.increase", 0));
                long costMoney = calculateCost(skill.getInt("prise.money", 0), nextLvl,
                        skill.getBoolean("use_additional_increase", true), skill.getDouble("prise.increase", 0));

                lore.add(MiniMessage.miniMessage().deserialize("<gray>Coût pour niveau <gold>" + nextLvl + " <gray>:"));
                if (costExp > 0)
                    lore.add(MiniMessage.miniMessage().deserialize(" <white>• <aqua>" + costExp + " Expérience"));
                if (costPts > 0)
                    lore.add(MiniMessage.miniMessage().deserialize(" <white>• <yellow>" + costPts + " Skill Points"));
                if (costMoney > 0)
                    lore.add(MiniMessage.miniMessage().deserialize(" <white>• <green>" + costMoney + " $"));

                lore.add(Component.empty());

                int reqLvl = skill.getInt("requirements.level", 0);
                String reqPerm = skill.getString("requirements.permission");
                int toolLvl = plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId);

                boolean canBuy = true;
                if (toolLvl < reqLvl) {
                    lore.add(MiniMessage.miniMessage().deserialize("<red>⚠ Requis: Outil Niv. " + reqLvl));
                    canBuy = false;
                }
                if (reqPerm != null && !player.hasPermission(reqPerm)) {
                    lore.add(MiniMessage.miniMessage().deserialize("<red>⚠ Requis: Permission spéciale"));
                    canBuy = false;
                }

                if (canBuy) {
                    lore.add(MiniMessage.miniMessage().deserialize("<green>▶ Cliquez pour améliorer !"));
                }
            } else {
                lore.add(MiniMessage.miniMessage().deserialize("<gold>Niveau Maximum Atteint !"));
            }

            meta.lore(lore);
            meta.getPersistentDataContainer().set(SKILL_ID_KEY, PersistentDataType.STRING, skillId);
            item.setItemMeta(meta);
        }
        return item;
    }

    public long calculateCost(long base, int level, boolean additional, double increasePercent) {
        if (level <= 1)
            return base;
        double factor = increasePercent / 100.0;
        if (additional) {
            return Math.round(base + (base * factor * (level - 1)));
        } else {
            return Math.round(base * Math.pow(1 + factor, level - 1));
        }
    }
}
