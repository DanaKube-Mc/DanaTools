package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomTool;
import com.danakube.danatools.model.ToolInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ToolXPListener implements Listener {

    private final DanaTools plugin;

    public ToolXPListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        ToolInstance tool = ToolInstance.fromItemStack(handItem);
        if (tool == null) {
            return;
        }

        Material blockType = event.getBlock().getType();
        CustomTool.BlockActivity activity = tool.getConfig().getBlockActivity(blockType);

        if (activity != null) {
            int xpGain = activity.getXp();
            if (xpGain > 0) {
                xpGain = plugin.getXpManager().applyLearningBoost(tool, xpGain);
                tool.addXP(xpGain, player);
            }
            CoreDropManager.checkAndDropCore(player, event.getBlock(), tool, activity);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        ToolInstance tool = ToolInstance.fromItemStack(handItem);
        if (tool == null) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Material originalType = clickedBlock.getType();
        Location blockLoc = clickedBlock.getLocation();

        boolean isHoe = handItem.getType().name().endsWith("_HOE");
        boolean isAxe = handItem.getType().name().endsWith("_AXE");

        if (isHoe && isTillable(originalType)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block block = blockLoc.getBlock();
                if (block.getType() == Material.FARMLAND) {
                    awardInteractXP(player, block, tool, originalType);
                }
            }, 1L);
        } else if (isAxe && isStrippable(originalType)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block block = blockLoc.getBlock();
                Material newType = block.getType();
                if (newType.name().equals("STRIPPED_" + originalType.name())) {
                    awardInteractXP(player, block, tool, originalType);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        ToolInstance tool = ToolInstance.fromItemStack(handItem);

        if (tool == null) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            tool = ToolInstance.fromItemStack(offHand);
        }

        if (tool == null) return;

        CustomTool.FishingActivity activity = tool.getConfig().getFishingActivity();
        if (activity != null) {
            int xpGain = activity.getXp();
            if (xpGain > 0) {
                xpGain = plugin.getXpManager().applyLearningBoost(tool, xpGain);
                tool.addXP(xpGain, player);
            }

            if (activity.hasCoreDrop()) {
                Location loc = (event.getCaught() != null) ? event.getCaught().getLocation() : player.getLocation();
                CoreDropManager.checkAndDropCore(player, loc, tool, activity.getCoreDrop());
            }
        }
    }

    private void awardInteractXP(Player player, Block block, ToolInstance tool, Material originalType) {
        CustomTool.BlockActivity activity = tool.getConfig().getBlockActivity(originalType);
        if (activity != null) {
            int xpGain = activity.getXp();
            if (xpGain > 0) {
                xpGain = plugin.getXpManager().applyLearningBoost(tool, xpGain);
                tool.addXP(xpGain, player);
            }
            CoreDropManager.checkAndDropCore(player, block, tool, activity);
        }
    }

    private boolean isTillable(Material mat) {
        return mat == Material.GRASS_BLOCK || mat == Material.DIRT || mat == Material.DIRT_PATH || mat == Material.ROOTED_DIRT;
    }

    private boolean isStrippable(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD") || name.endsWith("_STEM") || name.endsWith("_HYPHAE") || mat == Material.BAMBOO_BLOCK;
    }
}
