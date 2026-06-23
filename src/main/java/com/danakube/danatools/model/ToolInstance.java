package com.danakube.danatools.model;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.storage.ToolDataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ToolInstance {
    private final ItemStack item;
    private final CustomTool config;

    private ToolInstance(ItemStack item, CustomTool config) {
        this.item = item;
        this.config = config;
    }

    public static ToolInstance fromItemStack(ItemStack item) {
        if (item == null || !ToolDataStorage.isDanaTool(item)) {
            return null;
        }
        String toolId = ToolDataStorage.getToolId(item);
        CustomTool config = DanaTools.getInstance().getToolConfigManager().getTool(toolId);
        if (config == null) {
            return null;
        }
        return new ToolInstance(item, config);
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public CustomTool getConfig() {
        return this.config;
    }

    public String getToolId() {
        return config.getId();
    }

    public int getLevel() {
        return ToolDataStorage.getLevel(item);
    }

    public int getXp() {
        return ToolDataStorage.getXp(item);
    }

    public int getSlotsTotal() {
        return ToolDataStorage.getSlotsTotal(item);
    }

    public int getSlotsUsed() {
        return ToolDataStorage.getSlotsUsed(item);
    }

    public List<String> getModifiers() {
        return ToolDataStorage.getModifiers(item);
    }

    public boolean hasModifier(String modifierId) {
        return getModifiers().contains(modifierId);
    }

    public void addXP(int amount, Player player) {
        int maxLevel = config.getMaxLevel();
        int currentLevel = getLevel();

        if (currentLevel >= maxLevel) {
            return; 
        }

        int currentXp = getXp() + amount;
        int xpNeeded = DanaTools.getInstance().getXpManager().getXpRequiredFor(config, currentLevel);

        boolean leveledUp = false;
        while (currentXp >= xpNeeded && currentLevel < maxLevel) {
            currentXp -= xpNeeded;
            currentLevel++;
            leveledUp = true;
            xpNeeded = DanaTools.getInstance().getXpManager().getXpRequiredFor(config, currentLevel);
        }

        if (currentLevel >= maxLevel) {
            currentXp = 0;
        }

        ToolDataStorage.setXp(item, currentXp);

        if (leveledUp) {
            ToolDataStorage.setLevel(item, currentLevel);
            
            int newSlots = config.getSlotsForLevel(currentLevel);
            ToolDataStorage.setSlotsTotal(item, newSlots);

            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                
                Component levelUpMsg = DanaTools.getInstance().getLangManager().getMessage("xp.level_up", "{level}", currentLevel);
                player.sendMessage(levelUpMsg);
            }
        }

        updateLore();
    }

    public int getModifierLevel(String modifierId) {
        return ToolDataStorage.getModifierLevel(item, modifierId);
    }

    public boolean canApplyOrUpgradeModifier(CustomModifier modifier) {
        int currentLvl = getModifierLevel(modifier.getId());
        int targetLvl = currentLvl + 1;

        if (!modifier.hasLevel(targetLvl)) {
            return false;
        }

        CustomModifier.LevelSettings settings = modifier.getLevel(targetLvl);

        if (getLevel() < settings.getMinToolLevel()) {
            return false;
        }

        int currentCost = currentLvl > 0 ? modifier.getLevel(currentLvl).getSlotCost() : 0;
        int targetCost = settings.getSlotCost();
        int diffCost = targetCost - currentCost;

        int freeSlots = getSlotsTotal() - getSlotsUsed();
        if (freeSlots < diffCost) {
            return false;
        }

        if (modifier.getCompatibleTools() != null && !modifier.getCompatibleTools().isEmpty()) {
            if (!modifier.getCompatibleTools().contains(getToolId())) {
                return false;
            }
        }

        if (currentLvl == 0) {
            for (String activeModId : getModifiers()) {
                if (modifier.getIncompatibleModifiers().contains(activeModId)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void applyOrUpgradeModifier(CustomModifier modifier) {
        if (!canApplyOrUpgradeModifier(modifier)) return;

        int currentLvl = getModifierLevel(modifier.getId());
        int targetLvl = currentLvl + 1;

        CustomModifier.LevelSettings targetSettings = modifier.getLevel(targetLvl);
        int currentCost = currentLvl > 0 ? modifier.getLevel(currentLvl).getSlotCost() : 0;
        int diffCost = targetSettings.getSlotCost() - currentCost;

        ToolDataStorage.setModifierLevel(item, modifier.getId(), targetLvl);
        ToolDataStorage.setSlotsUsed(item, getSlotsUsed() + diffCost);
        updateLore();
    }

    public void updateLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String displayName = config.getDisplayName();
        meta.displayName(parseColor(displayName));

        List<String> rawLore = config.getLore();
        List<Component> formattedLore = new ArrayList<>();

        int level = getLevel();
        int xp = getXp();
        int maxLevel = config.getMaxLevel();
        int xpNeeded = DanaTools.getInstance().getXpManager().getXpRequiredFor(config, level);
        String xpStr = (level >= maxLevel) ? "MAX" : String.valueOf(xp);
        String maxXpStr = (level >= maxLevel) ? "MAX" : String.valueOf(xpNeeded);

        int slotsUsed = getSlotsUsed();
        int slotsTotal = getSlotsTotal();

        for (String line : rawLore) {
            if (line.contains("{modifiers_list}")) {
                List<String> activeModifiers = getModifiers();
                if (activeModifiers.isEmpty()) {
                    formattedLore.add(parseColor("&7 (Aucun modificateur)"));
                } else {
                    for (String modId : activeModifiers) {
                        CustomModifier modConfig = DanaTools.getInstance().getModifierConfigManager().getModifier(modId);
                        if (modConfig != null) {
                            int activeLvl = getModifierLevel(modId);
                            CustomModifier.LevelSettings settings = modConfig.getLevel(activeLvl);
                            if (settings != null) {
                                formattedLore.add(parseColor(settings.getDisplayName()));
                            } else {
                                formattedLore.add(parseColor("&7 - " + modId + " (Lvl " + activeLvl + ")"));
                            }
                        } else {
                            formattedLore.add(parseColor("&7 - " + modId));
                        }
                    }
                }
            } else {
                String processedLine = line
                        .replace("{level}", String.valueOf(level))
                        .replace("{xp}", xpStr)
                        .replace("{max_xp}", maxXpStr)
                        .replace("{slots_used}", String.valueOf(slotsUsed))
                        .replace("{slots_total}", String.valueOf(slotsTotal));
                formattedLore.add(parseColor(processedLine));
            }
        }

        meta.lore(formattedLore);
        
        if (config.getCustomModelData() > 0) {
            meta.setCustomModelData(config.getCustomModelData());
        }

        item.setItemMeta(meta);
    }

    public static Component parseColor(String text) {
        if (text == null) return Component.empty();
        if (text.contains("<") && text.contains(">")) {
            return MiniMessage.miniMessage().deserialize(text);
        } else {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }
}
