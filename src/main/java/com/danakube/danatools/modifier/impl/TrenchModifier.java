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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrenchModifier extends DanaModifier {

    private final Map<UUID, BlockFace> playerLastBlockFace = new HashMap<>();

    public TrenchModifier() {
        super("trench");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getBlockFace() != null) {
                playerLastBlockFace.put(event.getPlayer().getUniqueId(), event.getBlockFace());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLastBlockFace.remove(event.getPlayer().getUniqueId());
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

        BlockFace face = playerLastBlockFace.getOrDefault(player.getUniqueId(), BlockFace.UP);

        triggerTrenchMining(player, startBlock, face, handItem);
    }

    private void triggerTrenchMining(Player player, Block startBlock, BlockFace face, ItemStack toolItem) {
        processingCustomBreak.set(true);

        ToolInstance toolInstance = ToolInstance.fromItemStack(toolItem);
        int currentLvl = toolInstance != null ? toolInstance.getModifierLevel("trench") : 1;

        CustomModifier modConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("trench");
        int range = 1;
        if (modConfig != null) {
            CustomModifier.LevelSettings settings = modConfig.getLevel(currentLvl);
            if (settings != null) {
                range = settings.getBehaviorInt("range", 1);
            }
        }

        try {
            int startX = startBlock.getX();
            int startY = startBlock.getY();
            int startZ = startBlock.getZ();

            int dx1 = 0, dy1 = 0, dz1 = 0;
            int dx2 = 0, dy2 = 0, dz2 = 0;

            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                dx1 = 1; dz1 = 0;
                dx2 = 0; dz2 = 1;
            } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                dy1 = 1; dz1 = 0;
                dy2 = 0; dz2 = 1;
            } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                dx1 = 1; dy1 = 0;
                dx2 = 0; dy2 = 1;
            }

            for (int i = -range; i <= range; i++) {
                for (int j = -range; j <= range; j++) {
                    if (i == 0 && j == 0) continue;

                    int targetX = startX + (i * dx1) + (j * dx2);
                    int targetY = startY + (i * dy1) + (j * dy2);
                    int targetZ = startZ + (i * dz1) + (j * dz2);

                    Block targetBlock = startBlock.getWorld().getBlockAt(targetX, targetY, targetZ);
                    Material type = targetBlock.getType();

                    if (type.isAir() || type == Material.BEDROCK || targetBlock.isLiquid()) {
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
        } finally {
            processingCustomBreak.set(false);
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
