package com.babydev.danatools.menus;

import com.babydev.danatools.DanaTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import java.util.List;
import java.util.stream.Collectors;


public class ToolsMenu {

    private final DanaTools plugin;

    public ToolsMenu(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, Player target) {
        FileConfiguration config = plugin.getConfig();
        String path = "menus.tools";

        String titleString = config.getString(path + ".title", "<dark_aqua>Menu")
                + (viewer != target ? " (" + target.getName() + ")" : "");
        int size = config.getInt(path + ".size", 3) * 9;

        Component title = MiniMessage.miniMessage().deserialize(titleString);
        Inventory inventory = Bukkit.createInventory(new ToolsMenuHolder(), size, title);

        plugin.getToolsConfigs().forEach((toolId, toolConfig) -> {
            ItemStack item = createToolItemStack(target, toolId, toolConfig);
            if (item != null) {
                int slot = toolConfig.getInt("slot", 0);
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, item);
                }
            }
        });

        viewer.openInventory(inventory);
    }

    public void open(Player player) {
        open(player, player);
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


    public ItemStack createToolItemStack(Player player, String toolId, FileConfiguration toolConfig) {
        int level = plugin.getPlayerStatsManager().getToolLevel(player.getUniqueId(), toolId);
        int xp = plugin.getPlayerStatsManager().getToolXP(player.getUniqueId(), toolId);

        // --- Récupération de la config effective ---
        ConfigurationSection levelConfig = getEffectiveLevelSection(toolConfig, level);

        // --- Fallback sur les valeurs globales si rien n'est trouvé ---
        String matName = (levelConfig != null) ? levelConfig.getString("material")
                : toolConfig.getString("material", "STONE");
        if (matName == null)
            matName = toolConfig.getString("material", "STONE");

        Material material = Material.matchMaterial(matName);
        if (material == null)
            return null;

        String name = (levelConfig != null) ? levelConfig.getString("name") : toolConfig.getString("name", "");
        if (name == null)
            name = toolConfig.getString("name", "");

        List<String> loreStrings = (levelConfig != null) ? levelConfig.getStringList("lore")
                : toolConfig.getStringList("lore");
        if (loreStrings.isEmpty())
            loreStrings = toolConfig.getStringList("lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // --- Logique dynamique d'XP (Suivant use_equation_xp) ---
            int nextLevelXP;
            if (toolConfig.getBoolean("xp.use_equation_xp", true)) {
                int reqBase = toolConfig.getInt("xp.required_base", 100);
                int reqMult = toolConfig.getInt("xp.required_multiplier", 50);
                nextLevelXP = reqBase + (level * reqMult);
            } else {
                nextLevelXP = (levelConfig != null) ? levelConfig.getInt("xp_required", 100) : 100;
            }

            int maxLevel = toolConfig.getInt("max_level", 100);
            final String progressBar = (level < maxLevel) ? getProgressBar(xp, nextLevelXP,
                    toolConfig.getInt("visual.progress_bar_length", 10),
                    toolConfig.getString("visual.progress_bar_char", "|"),
                    toolConfig.getString("visual.progress_bar_filled", "<green>"),
                    toolConfig.getString("visual.progress_bar_empty", "<gray>")) : "";

            // --- Application des Placeholders ---
            double bonusPercent = plugin.getToolXPListener().getActiveBonusPercentage(player, toolId, toolConfig);
            int skillPoints = plugin.getPlayerStatsManager().getSkillPoints(player.getUniqueId(), toolId);
            String finalName = applyPlaceholders(name, toolId, level, xp, nextLevelXP, progressBar, bonusPercent,
                    skillPoints);
            if (!finalName.isEmpty()) {
                meta.displayName(MiniMessage.miniMessage().deserialize(finalName));
            }

            List<Component> lore = loreStrings.stream()
                    .map(s -> applyPlaceholders(s, toolId, level, xp, nextLevelXP, progressBar, bonusPercent,
                            skillPoints))
                    .map(s -> MiniMessage.miniMessage().deserialize(s))
                    .collect(Collectors.toList());

            lore.add(Component.empty());
            lore.add(MiniMessage.miniMessage().deserialize("<white>▶ <yellow>Clic Gauche: <gray>Récupérer/Ranger"));
            lore.add(MiniMessage.miniMessage().deserialize("<white>▶ <yellow>Clic Droit: <gray>Détails & Skills"));

            meta.lore(lore);

            // --- Custom Model Data ---
            if (levelConfig != null && levelConfig.contains("custom_model_data")) {
                meta.setCustomModelData(levelConfig.getInt("custom_model_data"));
            }else if (toolConfig.contains("custom_model_data")) {
                meta.setCustomModelData(toolConfig.getInt("custom_model_data"));
            }

            // --- Enchantments spécifiques au niveau ---
            if (levelConfig != null && levelConfig.contains("enchantments")) {
                ConfigurationSection enchants = levelConfig.getConfigurationSection("enchantments");
                if (enchants != null) {
                    for (String key : enchants.getKeys(false)) {
                        String enchName = enchants.getString(key + ".enchantment", "");
                        int enchLevel = enchants.getInt(key + ".level", 1);

                        if (!enchName.isEmpty()) {
                            Enchantment enchantment = Enchantment
                                    .getByKey(NamespacedKey.minecraft(enchName.toLowerCase()));
                            if (enchantment == null) {
                                enchantment = Enchantment.getByName(enchName.toUpperCase());
                            }

                            if (enchantment != null) {
                                meta.addEnchant(enchantment, enchLevel, true);
                            }
                        }
                    }
                }
            }

            // --- Unbreakable & Glow ---
            if (levelConfig != null) {
                if (levelConfig.contains("unbreakable"))
                    meta.setUnbreakable(levelConfig.getBoolean("unbreakable"));
                if (levelConfig.contains("glow"))
                    meta.setEnchantmentGlintOverride(levelConfig.getBoolean("glow"));
            }

            // --- SKILLS --- (Nouvelle fonctionnalité)
            if (toolConfig.getBoolean("use_skills", false) && toolConfig.contains("skills")) {
                ConfigurationSection skillsSection = toolConfig.getConfigurationSection("skills");
                if (skillsSection != null) {
                    for (String skillId : skillsSection.getKeys(false)) {
                        int skillLevelBought = plugin.getPlayerStatsManager().getSkillLevel(player.getUniqueId(),
                                toolId, skillId);
                        if (skillLevelBought > 0) {
                            ConfigurationSection skill = skillsSection.getConfigurationSection(skillId);
                            if (skill != null) {
                                if (skill.contains("enchantments")) {
                                    ConfigurationSection skillEnchants = skill.getConfigurationSection("enchantments");
                                    if (skillEnchants != null) {
                                        for (String eKey : skillEnchants.getKeys(false)) {
                                            String eName = skillEnchants.getString(eKey + ".enchantment", "");
                                            int baseLvl = skillEnchants.getInt(eKey + ".level", 0);
                                            int perLvl = skillEnchants.getInt(eKey + ".level_up", 1);

                                            int finalLvl = baseLvl + (skillLevelBought - 1) * perLvl;

                                            if (!eName.isEmpty() && finalLvl > 0) {
                                                Enchantment ench = Enchantment
                                                        .getByKey(NamespacedKey.minecraft(eName.toLowerCase()));
                                                if (ench == null)
                                                    ench = Enchantment.getByName(eName.toUpperCase());
                                                if (ench != null) {
                                                    int existing = meta.getEnchantLevel(ench);
                                                    meta.addEnchant(ench, existing + finalLvl, true);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (skill.contains("effects")) {
                                    ConfigurationSection skillEffects = skill.getConfigurationSection("effects");
                                    if (skillEffects != null) {
                                        for (String efKey : skillEffects.getKeys(false)) {
                                            String efName = skillEffects.getString(efKey + ".effect", "");
                                            int baseLvl = skillEffects.getInt(efKey + ".level", 0);
                                            int perLvl = skillEffects.getInt(efKey + ".level_up", 1);
                                            int finalLvl = baseLvl + (skillLevelBought - 1) * perLvl;

                                            if (!efName.isEmpty() && finalLvl > 0) {
                                                // On verra pour l'application passive plus tard, pour le moment on peut
                                                // l'afficher dans le lore ou le PDC
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Hide Enchants ---
            if (toolConfig.getBoolean("hide_enchants", false)) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            meta.getPersistentDataContainer().set(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING, toolId);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getProgressBar(int current, int max, int length, String character, String fullColor,
            String emptyColor) {
        float percent = (float) current / max;
        int progressBars = (int) (length * percent);
        int leftOver = length - progressBars;

        StringBuilder sb = new StringBuilder();
        sb.append(fullColor);
        for (int i = 0; i < progressBars; i++) {
            sb.append(character);
        }
        sb.append(emptyColor);
        for (int i = 0; i < leftOver; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    private String applyPlaceholders(String text, String toolId, int level, int xp, int maxXP, String progressBar,
            double bonus, int skillPoints) {
        if (text == null)
            return "";
        String bonusStr = (bonus >= 0 ? "+" : "") + (int) bonus + "%";
        return text.replace("%level%", String.valueOf(level))
                .replace("%xp%", String.valueOf(xp))
                .replace("%max_xp%", String.valueOf(maxXP))
                .replace("%progress_bar%", progressBar)
                .replace("%bonus%", bonusStr)
                .replace("%skill_points%", String.valueOf(skillPoints))
                .replace("%" + toolId + "_level%", String.valueOf(level))
                .replace("%" + toolId + "_xp%", String.valueOf(xp));
    }
}