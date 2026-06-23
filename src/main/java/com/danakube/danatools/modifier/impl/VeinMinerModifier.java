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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class VeinMinerModifier extends DanaModifier {

    public VeinMinerModifier() {
        super("vein_miner");
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
        ItemStack handItem = player.getInventory().getItemInMainHand();

        triggerVeinMining(player, startBlock, handItem);
    }

    private boolean isVeinMinerable(Material material) {
        String name = material.name();
        return name.endsWith("_ORE") || name.endsWith("_LOG") || name.endsWith("_WOOD");
    }

    private void triggerVeinMining(Player player, Block startBlock, ItemStack toolItem) {
        Material targetMaterial = startBlock.getType();
        if (!isVeinMinerable(targetMaterial)) {
            return;
        }

        ToolInstance toolInstance = ToolInstance.fromItemStack(toolItem);
        int currentLvl = toolInstance != null ? toolInstance.getModifierLevel("vein_miner") : 1;

        CustomModifier modConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("vein_miner");
        int maxBlocks = 64;
        if (modConfig != null) {
            CustomModifier.LevelSettings settings = modConfig.getLevel(currentLvl);
            if (settings != null) {
                maxBlocks = settings.getBehaviorInt("max-blocks", 64);
            }
        }

        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        int brokenCount = 0;

        processingCustomBreak.set(true);

        try {
            while (!queue.isEmpty() && brokenCount < maxBlocks) {
                Block current = queue.poll();

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;

                            Block neighbor = current.getRelative(x, y, z);
                            if (neighbor.getType() == targetMaterial && !visited.contains(neighbor)) {
                                visited.add(neighbor);

                                BlockBreakEvent virtualEvent = new BlockBreakEvent(neighbor, player);
                                Bukkit.getPluginManager().callEvent(virtualEvent);

                                if (!virtualEvent.isCancelled()) {
                                    DropManager.breakBlock(player, neighbor, toolItem, virtualEvent.getExpToDrop());
                                    brokenCount++;

                                    applyDurabilityDamage(player, toolItem);

                                    if (toolItem.getType() == Material.AIR || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                        return;
                                    }

                                    queue.add(neighbor);
                                    
                                    if (brokenCount >= maxBlocks) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            processingCustomBreak.set(false);
        }
    }

    private void applyDurabilityDamage(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta() instanceof Damageable damageable) {
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
