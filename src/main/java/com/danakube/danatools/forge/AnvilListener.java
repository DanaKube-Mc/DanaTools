package com.danakube.danatools.forge;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomTool;
import com.danakube.danatools.model.ToolInstance;
import com.danakube.danatools.storage.ToolDataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;

import java.util.HashMap;
import java.util.Map;

public class AnvilListener implements Listener {

    private final DanaTools plugin;

    public AnvilListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);

        if (left == null || left.getType().isAir()) {
            return;
        }

        if (isEnchantedBook(left) && isEnchantedBook(right)) {
            handleBookBook(event, left, right);
            return;
        }

        if (ToolDataStorage.isDanaTool(left) && isEnchantedBook(right)) {
            handleToolBook(event, left, right);
            return;
        }

        if (ToolDataStorage.isDanaTool(left) && ToolDataStorage.isDanaTool(right)) {
            handleToolTool(event, left, right);
            return;
        }
    }

    private boolean isEnchantedBook(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_BOOK;
    }

    private void handleBookBook(PrepareAnvilEvent event, ItemStack left, ItemStack right) {
        EnchantmentStorageMeta leftMeta = (EnchantmentStorageMeta) left.getItemMeta();
        EnchantmentStorageMeta rightMeta = (EnchantmentStorageMeta) right.getItemMeta();
        if (leftMeta == null || rightMeta == null) return;

        Map<Enchantment, Integer> leftEnchants = leftMeta.getStoredEnchants();
        Map<Enchantment, Integer> rightEnchants = rightMeta.getStoredEnchants();

        Map<Enchantment, Integer> combinedEnchants = new HashMap<>(leftEnchants);
        boolean changed = false;
        int xpCost = 2;

        for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int rightLvl = entry.getValue();

            if (combinedEnchants.containsKey(ench)) {
                int leftLvl = combinedEnchants.get(ench);
                if (leftLvl == rightLvl) {
                    combinedEnchants.put(ench, leftLvl + 1);
                    changed = true;
                    xpCost += 2;
                } else if (rightLvl > leftLvl) {
                    combinedEnchants.put(ench, rightLvl);
                    changed = true;
                    xpCost += 1;
                }
            } else {
                combinedEnchants.put(ench, rightLvl);
                changed = true;
                xpCost += 1;
            }
        }

        boolean hasCustomLevel = false;
        for (Map.Entry<Enchantment, Integer> entry : combinedEnchants.entrySet()) {
            if (entry.getValue() > entry.getKey().getMaxLevel()) {
                hasCustomLevel = true;
                break;
            }
        }

        if (!hasCustomLevel) {
            return;
        }

        AnvilView anvilView = event.getView() instanceof AnvilView ? (AnvilView) event.getView() : null;
        String renameText = anvilView != null ? anvilView.getRenameText() : null;
        boolean renamed = false;
        if (renameText != null && !renameText.trim().isEmpty()) {
            renamed = true;
            xpCost += 1;
        }

        if (!changed && !renamed) {
            event.setResult(null);
            return;
        }

        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta resultMeta = (EnchantmentStorageMeta) result.getItemMeta();
        if (resultMeta != null) {
            for (Map.Entry<Enchantment, Integer> entry : combinedEnchants.entrySet()) {
                resultMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            }
            if (renamed) {
                resultMeta.displayName(ToolInstance.parseColor(renameText));
            }
            if (resultMeta instanceof Repairable repairable) {
                repairable.setRepairCost(0);
            }

            resultMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            resultMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            java.util.List<Component> bookLore = new java.util.ArrayList<>();
            for (Map.Entry<Enchantment, Integer> entry : combinedEnchants.entrySet()) {
                Enchantment ench = entry.getKey();
                int lvl = entry.getValue();
                Component line = Component.translatable(ench)
                        .append(Component.text(" " + ToolInstance.toRoman(lvl)))
                        .color(ench.isCursed() ? net.kyori.adventure.text.format.NamedTextColor.RED : net.kyori.adventure.text.format.NamedTextColor.GRAY);
                bookLore.add(line);
            }
            resultMeta.lore(bookLore);

            result.setItemMeta(resultMeta);
        }

        xpCost = Math.min(30, xpCost);
        event.setResult(result);
        if (anvilView != null) {
            anvilView.setRepairCost(xpCost);
        }
    }

    private void handleToolBook(PrepareAnvilEvent event, ItemStack left, ItemStack right) {
        String toolId = ToolDataStorage.getToolId(left);
        CustomTool toolConfig = plugin.getToolConfigManager().getTool(toolId);
        if (toolConfig == null) return;

        ItemMeta leftMeta = left.getItemMeta();
        EnchantmentStorageMeta rightMeta = (EnchantmentStorageMeta) right.getItemMeta();
        if (leftMeta == null || rightMeta == null) return;

        Map<Enchantment, Integer> leftEnchants = left.getEnchantments();
        Map<Enchantment, Integer> rightEnchants = rightMeta.getStoredEnchants();

        Map<Enchantment, Integer> combinedEnchants = new HashMap<>(leftEnchants);
        boolean changed = false;
        int xpCost = 2;

        for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int rightLvl = entry.getValue();

            if (!ench.canEnchantItem(left)) {
                continue;
            }

            boolean incompatible = false;
            for (Enchantment active : combinedEnchants.keySet()) {
                if (ench != active && ench.conflictsWith(active)) {
                    incompatible = true;
                    break;
                }
            }
            if (incompatible) {
                continue;
            }

            int leftLvl = combinedEnchants.getOrDefault(ench, 0);
            int targetLvl = leftLvl;

            if (leftLvl > 0) {
                if (leftLvl == rightLvl) {
                    targetLvl = leftLvl + 1;
                } else {
                    targetLvl = Math.max(leftLvl, rightLvl);
                }
            } else {
                targetLvl = rightLvl;
            }

            int limit = toolConfig.getEnchantmentLimit(ench);
            if (targetLvl > limit) {
                targetLvl = limit;
            }

            if (targetLvl > leftLvl) {
                combinedEnchants.put(ench, targetLvl);
                changed = true;
                xpCost += 2;
            }
        }

        AnvilView anvilView = event.getView() instanceof AnvilView ? (AnvilView) event.getView() : null;
        String renameText = anvilView != null ? anvilView.getRenameText() : null;
        boolean renamed = false;
        if (renameText != null && !renameText.trim().isEmpty()) {
            String currentName = getPlainName(left);
            if (!renameText.equalsIgnoreCase(currentName)) {
                renamed = true;
                xpCost += 1;
            }
        }

        if (!changed && !renamed) {
            event.setResult(null);
            return;
        }

        ItemStack result = left.clone();
        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta != null) {
            for (Enchantment active : result.getEnchantments().keySet()) {
                resultMeta.removeEnchant(active);
            }
            for (Map.Entry<Enchantment, Integer> entry : combinedEnchants.entrySet()) {
                resultMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
            if (resultMeta instanceof Repairable repairable) {
                repairable.setRepairCost(0);
            }
            result.setItemMeta(resultMeta);
        }

        if (renamed) {
            ToolDataStorage.setCustomName(result, renameText);
        }

        ToolInstance tool = ToolInstance.fromItemStack(result);
        if (tool != null) {
            tool.updateLore();
            result = tool.getItemStack();
        }

        xpCost = Math.min(30, xpCost);
        event.setResult(result);
        if (anvilView != null) {
            anvilView.setRepairCost(xpCost);
        }
    }

    private void handleToolTool(PrepareAnvilEvent event, ItemStack left, ItemStack right) {
        String leftId = ToolDataStorage.getToolId(left);
        String rightId = ToolDataStorage.getToolId(right);
        if (leftId == null || !leftId.equals(rightId)) {
            event.setResult(null);
            return;
        }

        CustomTool toolConfig = plugin.getToolConfigManager().getTool(leftId);
        if (toolConfig == null) return;

        ItemMeta leftMeta = left.getItemMeta();
        ItemMeta rightMeta = right.getItemMeta();
        if (leftMeta == null || rightMeta == null) return;

        Map<Enchantment, Integer> leftEnchants = left.getEnchantments();
        Map<Enchantment, Integer> rightEnchants = right.getEnchantments();

        Map<Enchantment, Integer> combinedEnchants = new HashMap<>(leftEnchants);
        boolean changed = false;
        int xpCost = 2;

        for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int rightLvl = entry.getValue();

            boolean incompatible = false;
            for (Enchantment active : combinedEnchants.keySet()) {
                if (ench != active && ench.conflictsWith(active)) {
                    incompatible = true;
                    break;
                }
            }
            if (incompatible) {
                continue;
            }

            int leftLvl = combinedEnchants.getOrDefault(ench, 0);
            int targetLvl = leftLvl;

            if (leftLvl > 0) {
                if (leftLvl == rightLvl) {
                    targetLvl = leftLvl + 1;
                } else {
                    targetLvl = Math.max(leftLvl, rightLvl);
                }
            } else {
                targetLvl = rightLvl;
            }

            int limit = toolConfig.getEnchantmentLimit(ench);
            if (targetLvl > limit) {
                targetLvl = limit;
            }

            if (targetLvl > leftLvl) {
                combinedEnchants.put(ench, targetLvl);
                changed = true;
                xpCost += 2;
            }
        }

        boolean durabilityRepaired = false;
        short maxDurability = left.getType().getMaxDurability();
        if (leftMeta instanceof org.bukkit.inventory.meta.Damageable leftD && rightMeta instanceof org.bukkit.inventory.meta.Damageable rightD) {
            int leftDamage = leftD.getDamage();
            int rightDamage = rightD.getDamage();
            if (leftDamage > 0) {
                int repValue = (maxDurability - leftDamage) + (maxDurability - rightDamage) + (int) (maxDurability * 0.12);
                int newDamage = Math.max(0, maxDurability - repValue);
                if (newDamage < leftDamage) {
                    durabilityRepaired = true;
                    xpCost += 2;
                }
            }
        }

        AnvilView anvilView = event.getView() instanceof AnvilView ? (AnvilView) event.getView() : null;
        String renameText = anvilView != null ? anvilView.getRenameText() : null;
        boolean renamed = false;
        if (renameText != null && !renameText.trim().isEmpty()) {
            String currentName = getPlainName(left);
            if (!renameText.equalsIgnoreCase(currentName)) {
                renamed = true;
                xpCost += 1;
            }
        }

        if (!changed && !durabilityRepaired && !renamed) {
            event.setResult(null);
            return;
        }

        ItemStack result = left.clone();
        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta != null) {
            for (Enchantment active : result.getEnchantments().keySet()) {
                resultMeta.removeEnchant(active);
            }
            for (Map.Entry<Enchantment, Integer> entry : combinedEnchants.entrySet()) {
                resultMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            if (durabilityRepaired && resultMeta instanceof org.bukkit.inventory.meta.Damageable resultD) {
                int leftDamage = ((org.bukkit.inventory.meta.Damageable) leftMeta).getDamage();
                int rightDamage = ((org.bukkit.inventory.meta.Damageable) rightMeta).getDamage();
                int repValue = (maxDurability - leftDamage) + (maxDurability - rightDamage) + (int) (maxDurability * 0.12);
                resultD.setDamage(Math.max(0, maxDurability - repValue));
            }

            if (resultMeta instanceof Repairable repairable) {
                repairable.setRepairCost(0);
            }
            result.setItemMeta(resultMeta);
        }

        if (renamed) {
            ToolDataStorage.setCustomName(result, renameText);
        }

        ToolInstance tool = ToolInstance.fromItemStack(result);
        if (tool != null) {
            tool.updateLore();
            result = tool.getItemStack();
        }

        xpCost = Math.min(30, xpCost);
        event.setResult(result);
        if (anvilView != null) {
            anvilView.setRepairCost(xpCost);
        }
    }

    private String getPlainName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return "";
        ItemMeta meta = item.getItemMeta();
        Component comp = meta.displayName();
        if (comp == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(comp);
    }
}
