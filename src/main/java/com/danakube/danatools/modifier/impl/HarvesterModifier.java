package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.ToolInstance;
import com.danakube.danatools.modifier.DanaModifier;
import com.danakube.danatools.modifier.DropManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;

public class HarvesterModifier extends DanaModifier {

    public HarvesterModifier() {
        super("harvester");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (processingCustomBreak.get()) {
            return;
        }

        Player player = event.getPlayer();
        if (!isEquipped(player)) {
            return;
        }

        Block startBlock = event.getBlock();
        ItemStack toolItem = player.getInventory().getItemInMainHand();

        ToolInstance tool = ToolInstance.fromItemStack(toolItem);
        int level = tool != null ? tool.getModifierLevel("harvester") : 1;

        CustomModifier modifierConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("harvester");
        if (modifierConfig == null) return;
        CustomModifier.LevelSettings settings = modifierConfig.getLevel(level);
        if (settings == null) return;

        int range = settings.getBehaviorInt("range", 1);
        boolean onlyMature = true;
        Object onlyMatureObj = settings.getBehaviorSettings().get("only-mature");
        if (onlyMatureObj instanceof Boolean) {
            onlyMature = (Boolean) onlyMatureObj;
        }

        List<String> cropsList = new ArrayList<>();
        Object cropsObj = settings.getBehaviorSettings().get("crops");
        if (cropsObj instanceof List<?>) {
            for (Object item : (List<?>) cropsObj) {
                cropsList.add(item.toString());
            }
        }

        Material startType = startBlock.getType();
        if (!cropsList.contains(startType.name())) {
            return;
        }

        if (onlyMature && startBlock.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() < ageable.getMaximumAge()) {
                return;
            }
        }

        processingCustomBreak.set(true);
        try {
            triggerAreaHarvesting(player, startBlock, range, cropsList, onlyMature, toolItem);
        } finally {
            processingCustomBreak.set(false);
        }
    }

    private void triggerAreaHarvesting(Player player, Block startBlock, int range, List<String> cropsList, boolean onlyMature, ItemStack toolItem) {
        int startX = startBlock.getX();
        int startY = startBlock.getY();
        int startZ = startBlock.getZ();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                if (x == 0 && z == 0) continue;

                Block targetBlock = startBlock.getWorld().getBlockAt(startX + x, startY, startZ + z);
                Material targetType = targetBlock.getType();

                if (cropsList.contains(targetType.name())) {
                    boolean isMature = true;
                    if (targetBlock.getBlockData() instanceof Ageable ageable) {
                        if (ageable.getAge() < ageable.getMaximumAge()) {
                            isMature = false;
                        }
                    }

                    if (onlyMature && !isMature) {
                        continue;
                    }

                    BlockBreakEvent virtualEvent = new BlockBreakEvent(targetBlock, player);
                    Bukkit.getPluginManager().callEvent(virtualEvent);

                    if (!virtualEvent.isCancelled()) {
                        DropManager.breakBlock(player, targetBlock, toolItem, virtualEvent.getExpToDrop());
                        applyDurabilityDamage(player, toolItem);

                        if (toolItem.getType() == Material.AIR || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void applyDurabilityDamage(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta() instanceof Damageable damageable) {
            if (damageable.isUnbreakable()) return;
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (Math.random() < (1.0 / (unbreakingLevel + 1))) {
                int newDamage = damageable.getDamage() + 1;
                if (newDamage >= item.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(newDamage);
                    item.setItemMeta(damageable);
                }
            }
        }
    }
}
