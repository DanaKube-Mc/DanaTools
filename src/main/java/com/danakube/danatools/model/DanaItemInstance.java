package com.danakube.danatools.model;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.event.ToolXpGainEvent;
import com.danakube.danatools.storage.ToolDataStorage;
import com.danakube.danatools.utils.ProgressBarUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DanaItemInstance {
    private static final ThreadLocal<Boolean> sharingResonance = ThreadLocal.withInitial(() -> false);

    private final ItemStack item;
    private final CustomTool config;

    private DanaItemInstance(ItemStack item, CustomTool config) {
        this.item = item;
        this.config = config;
    }

    public static DanaItemInstance fromItemStack(ItemStack item) {
        if (item == null || !ToolDataStorage.isDanaTool(item)) {
            return null;
        }
        String toolId = ToolDataStorage.getToolId(item);
        CustomTool config = DanaTools.getInstance().getToolConfigManager().getTool(toolId);
        if (config == null) {
            return null;
        }
        return new DanaItemInstance(item, config);
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
    
    public boolean hasBehavior(String behaviorType) {
        for (String modifierId : getModifiers()) {
            CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier(modifierId);
            if (modifier != null) {
                int level = getModifierLevel(modifierId);
                CustomModifier.LevelSettings settings = modifier.getLevel(level);
                if (settings != null && behaviorType.equalsIgnoreCase(settings.getBehaviorType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getBehaviorLevel(String behaviorType) {
        int maxLvl = 0;
        for (String modifierId : getModifiers()) {
            CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier(modifierId);
            if (modifier != null) {
                int level = getModifierLevel(modifierId);
                CustomModifier.LevelSettings settings = modifier.getLevel(level);
                if (settings != null && behaviorType.equalsIgnoreCase(settings.getBehaviorType())) {
                    maxLvl = Math.max(maxLvl, level);
                }
            }
        }
        return maxLvl;
    }

    public CustomModifier getBehaviorModifier(String behaviorType) {
        for (String modifierId : getModifiers()) {
            CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier(modifierId);
            if (modifier != null) {
                int level = getModifierLevel(modifierId);
                CustomModifier.LevelSettings settings = modifier.getLevel(level);
                if (settings != null && behaviorType.equalsIgnoreCase(settings.getBehaviorType())) {
                    return modifier;
                }
            }
        }
        return null;
    }

    public void addXP(double amount, Player player) {
        if (amount <= 0) return;

        int maxLevel = config.getMaxLevel();
        int currentLevel = getLevel();

        if (currentLevel >= maxLevel) {
            return;
        }

        double currentFraction = ToolDataStorage.getXpFraction(item);
        double total = currentFraction + amount;
        int wholePoints = (int) Math.floor(total);
        double remainingFraction = total - wholePoints;

        ToolDataStorage.setXpFraction(item, remainingFraction);

        if (wholePoints > 0) {
            applyWholeXpGain(wholePoints, player);
        }
    }

    public void addXP(int amount, Player player) {
        addXP((double) amount, player);
    }

    private void applyWholeXpGain(int amount, Player player) {
        int maxLevel = config.getMaxLevel();
        int currentLevel = getLevel();

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
            ToolDataStorage.setXpFraction(item, 0.0);
        }

        ToolDataStorage.setXp(item, currentXp);

        if (leveledUp) {
            ToolDataStorage.setLevel(item, currentLevel);
            
            int newSlots = config.getSlotsForLevel(currentLevel);
            ToolDataStorage.setSlotsTotal(item, newSlots);

            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                
                Component levelUpMsg = DanaTools.getInstance().getLangManager().getMessage("xp.level_up", "{level}", currentLevel);
                player.sendMessage(levelUpMsg);
            }
        }

        updateLore();

        if (player != null && !isEquippedArmor(player) && isHeldInMainHand(player)) {
            shareResonanceXP(amount, player);
        }

        if (player != null && amount > 0) {
            Bukkit.getPluginManager().callEvent(new ToolXpGainEvent(player, amount));
        }
    }

    private boolean isEquippedArmor(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack armorPiece : armor) {
            if (armorPiece != null && armorPiece.equals(this.item)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHeldInMainHand(Player player) {
        return player.getInventory().getItemInMainHand().equals(this.item);
    }

    private void shareResonanceXP(double amount, Player player) {
        if (player == null || amount <= 0) return;
        if (sharingResonance.get()) return;

        sharingResonance.set(true);
        try {
            List<ItemStack> candidates = new ArrayList<>();

            // 1. Armures équipées
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack piece : armor) {
                if (piece != null && !piece.getType().isAir()) {
                    candidates.add(piece);
                }
            }

            // 2. Seconde main (Off-hand), si elle n'est pas l'item source
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand != null && !offHand.getType().isAir() && !offHand.equals(this.item)) {
                candidates.add(offHand);
            }

            // 3. Main principale, si elle n'est pas l'item source (sécurité si l'XP venait de l'off-hand ou d'une armure)
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand != null && !mainHand.getType().isAir() && !mainHand.equals(this.item)) {
                candidates.add(mainHand);
            }

            CustomModifier modConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("resonance");

            for (ItemStack candidate : candidates) {
                DanaItemInstance candidateInstance = DanaItemInstance.fromItemStack(candidate);
                if (candidateInstance != null && candidateInstance.hasModifier("resonance")) {
                    int lvl = candidateInstance.getModifierLevel("resonance");
                    if (modConfig != null) {
                        CustomModifier.LevelSettings settings = modConfig.getLevel(lvl);
                        if (settings != null) {
                            double sharePercent = settings.getBehaviorDouble("xp-share-percent", 0.0);
                            if (sharePercent <= 0) {
                                sharePercent = settings.getBehaviorInt("xp-share-percent", 0);
                            }
                            if (sharePercent > 0) {
                                double sharedXp = amount * (sharePercent / 100.0);
                                if (sharedXp > 0) {
                                    candidateInstance.addXP(sharedXp, player);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            sharingResonance.set(false);
        }
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

        Map<String, Integer> allowedMods = getConfig().getAllowedModifiers();
        if (allowedMods == null || !allowedMods.containsKey(modifier.getId())) {
            return false;
        }
        int maxLvl = allowedMods.get(modifier.getId());
        if (targetLvl > maxLvl) {
            return false;
        }

        if (currentLvl == 0) {
            for (String activeModId : getModifiers()) {
                if (modifier.getIncompatibleModifiers().contains(activeModId)) {
                    return false;
                }
            }
        }

        if (modifier.getId().equals("auto_smelt") || modifier.getId().equals("compactor") || modifier.getId().equals("auto_sell")) {
            if (item.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                return false;
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

        String customName = ToolDataStorage.getCustomName(item);
        if (customName != null) {
            meta.displayName(parseColor(customName));
        } else {
            String displayName = config.getDisplayName();
            meta.displayName(parseColor(displayName));
        }

        List<Component> formattedLore = new ArrayList<>();

        Map<Enchantment, Integer> enchantments = meta.getEnchants();
        if (!enchantments.isEmpty()) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment ench = entry.getKey();
                int level = entry.getValue();
                Component lineComp;
                if (ench.getKey().getNamespace().equals("minecraft")) {
                    lineComp = Component.translatable(ench)
                            .append(Component.text(" " + toRoman(level)))
                            .color(ench.isCursed() ? NamedTextColor.RED : NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                } else {
                    lineComp = ench.displayName(level)
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                }
                formattedLore.add(lineComp);
            }
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.removeItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
        }

        List<String> rawLore = config.getLore();

        int level = getLevel();
        int xp = getXp();
        int maxLevel = config.getMaxLevel();
        int xpNeeded = DanaTools.getInstance().getXpManager().getXpRequiredFor(config, level);
        String xpStr = (level >= maxLevel) ? "MAX" : String.valueOf(xp);
        String maxXpStr = (level >= maxLevel) ? "MAX" : String.valueOf(xpNeeded);

        int slotsUsed = getSlotsUsed();
        int slotsTotal = getSlotsTotal();

        int percent;
        String progressBarStr;
        if (level >= maxLevel) {
            percent = 100;
            progressBarStr = ProgressBarUtils.generateProgressBar(100, 100);
        } else {
            percent = xpNeeded > 0 ? (int) Math.round(((double) xp / xpNeeded) * 100) : 0;
            percent = Math.max(0, Math.min(100, percent));
            progressBarStr = ProgressBarUtils.generateProgressBar(xp, xpNeeded);
        }

        for (String line : rawLore) {
            if (line.contains("{modifiers_list}")) {
                List<String> activeModifiers = getModifiers();
                if (activeModifiers.isEmpty()) {
                    String noModMsgStr = config.getNoModifierMessage();
                    Component noModMsg;
                    if (noModMsgStr != null) {
                        noModMsg = parseColor(noModMsgStr);
                    } else {
                        noModMsg = DanaTools.getInstance().getLangManager().getMessage("modifiers.none");
                    }
                    formattedLore.add(noModMsg);
                } else {
                    for (String modId : activeModifiers) {
                        CustomModifier modConfig = DanaTools.getInstance().getModifierConfigManager().getModifier(modId);
                        if (modConfig != null) {
                            int activeLvl = getModifierLevel(modId);
                            CustomModifier.LevelSettings settings = modConfig.getLevel(activeLvl);
                            if (settings != null) {
                                formattedLore.add(parseColor(settings.getDisplayName()));
                            } else {
                                Component formatted = DanaTools.getInstance().getLangManager().getMessage("modifiers.format_with_level", "{modifier}", modId, "{level}", activeLvl);
                                formattedLore.add(formatted);
                            }
                        } else {
                            Component formatted = DanaTools.getInstance().getLangManager().getMessage("modifiers.format_no_level", "{modifier}", modId);
                            formattedLore.add(formatted);
                        }
                    }
                }
            } else {
                String processedLine = line
                        .replace("{level}", String.valueOf(level))
                        .replace("{xp}", xpStr)
                        .replace("{max_xp}", maxXpStr)
                        .replace("{slots_used}", String.valueOf(slotsUsed))
                        .replace("{slots_total}", String.valueOf(slotsTotal))
                        .replace("{progress_bar}", progressBarStr)
                        .replace("{percent}", String.valueOf(percent));
                formattedLore.add(parseColor(processedLine));
            }
        }

        meta.lore(formattedLore);
        
        if (config.getCustomModelData() > 0) {
            meta.setCustomModelData(config.getCustomModelData());
        }

        if (hasModifier("unbreakable")) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        } else {
            meta.setUnbreakable(false);
            meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        item.setItemMeta(meta);
    }

    public static String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romanLetters = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }

    public static Component parseColor(String text) {
        if (text == null) return Component.empty();
        Component component;
        if (text.contains("<") && text.contains(">")) {
            component = MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(text));
        } else {
            component = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
        return component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static String convertLegacyToMiniMessage(String text) {
        if (text == null) return null;
        text = text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
        text = text
                .replace("&A", "<green>")
                .replace("&B", "<aqua>")
                .replace("&C", "<red>")
                .replace("&D", "<light_purple>")
                .replace("&E", "<yellow>")
                .replace("&F", "<white>")
                .replace("&K", "<obfuscated>")
                .replace("&L", "<bold>")
                .replace("&M", "<strikethrough>")
                .replace("&N", "<underlined>")
                .replace("&O", "<italic>")
                .replace("&R", "<reset>");
        return text;
    }
}
