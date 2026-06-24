package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool.BlockActivity;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import com.danakube.danatools.progression.CoreDropManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class TillerModifier extends DanaModifier {

    public TillerModifier() {
        super("tiller");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getBlockFace() != BlockFace.UP) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!isEquipped(player)) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !isTillable(clickedBlock.getType())) return;

        ItemStack hoe = event.getItem();
        if (hoe == null) return;

        DanaItemInstance tool = DanaItemInstance.fromItemStack(hoe);
        int level = tool != null ? tool.getModifierLevel("tiller") : 1;

        CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier("tiller");
        int range = 1;
        if (modifier != null) {
            CustomModifier.LevelSettings settings = modifier.getLevel(level);
            if (settings != null) {
                range = settings.getBehaviorInt("range", 1);
            }
        }

        event.setCancelled(true);
        
        processingCustomBreak.set(true);
        try {
            triggerAreaTilling(player, clickedBlock, range, hoe);
        } finally {
            processingCustomBreak.set(false);
        }
    }

    private boolean isTillable(Material mat) {
        return mat == Material.GRASS_BLOCK || mat == Material.DIRT || mat == Material.DIRT_PATH || mat == Material.ROOTED_DIRT;
    }

    private void triggerAreaTilling(Player player, Block centerBlock, int range, ItemStack tool) {
        int startX = centerBlock.getX();
        int startY = centerBlock.getY();
        int startZ = centerBlock.getZ();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                Block targetBlock = centerBlock.getWorld().getBlockAt(startX + x, startY, startZ + z);
                
                if (isTillable(targetBlock.getType())) {
                    Block blockAbove = targetBlock.getRelative(BlockFace.UP);
                    if (!blockAbove.getType().isAir() && blockAbove.getType().isSolid()) {
                        continue;
                    }

                    BlockBreakEvent virtualEvent = new BlockBreakEvent(targetBlock, player);
                    Bukkit.getPluginManager().callEvent(virtualEvent);

                    if (!virtualEvent.isCancelled()) {
                        Material originalType = targetBlock.getType();
                        targetBlock.setType(Material.FARMLAND);

                        DanaItemInstance toolInstance = DanaItemInstance.fromItemStack(tool);
                        if (toolInstance != null) {
                            BlockActivity activity = toolInstance.getConfig().getBlockActivity(originalType);
                            if (activity != null) {
                                int xpGain = activity.getXp();
                                if (xpGain > 0) {
                                    xpGain = DanaTools.getInstance().getXpManager().applyLearningBoost(toolInstance, xpGain);
                                    toolInstance.addXP(xpGain, player);
                                }
                                CoreDropManager.checkAndDropCore(player, targetBlock.getLocation().add(0.5, 0.5, 0.5), toolInstance, activity.getCoreDrop());
                            }
                        }
                        
                        if (blockAbove.getType() != Material.AIR) {
                            blockAbove.setType(Material.AIR);
                        }

                        applyDurabilityDamage(player, tool);

                        if (tool.getType() == Material.AIR || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            return;
                        }
                    }
                }
            }
        }
        
        centerBlock.getWorld().playSound(centerBlock.getLocation(), Sound.ITEM_HOE_TILL, 1.0f, 1.0f);
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
