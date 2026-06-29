package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import com.danakube.danatools.modifier.DropManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoReplantModifier extends DanaModifier {

    private final Map<Location, BlockData> matureCropsBroken = new HashMap<>();

    public AutoReplantModifier() {
        super("auto_replant");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(hand);

        if (tool != null && tool.hasModifier("auto_replant")) {
            if (DropManager.isReplantableCrop(block.getType())) {
                BlockData blockData = block.getBlockData();
                if (blockData instanceof Ageable ageable) {
                    if (ageable.getAge() == ageable.getMaximumAge()) {
                        Location loc = block.getLocation();
                        matureCropsBroken.put(loc, blockData.clone());
                        Bukkit.getScheduler().runTask(DanaTools.getInstance(), () -> {
                            matureCropsBroken.remove(loc);
                        });
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Location loc = event.getBlock().getLocation();
        BlockData originalData = matureCropsBroken.remove(loc);
        if (originalData == null) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(hand);
        if (tool == null || !tool.hasModifier("auto_replant")) return;

        Material seedMaterial = DropManager.getRequiredSeed(originalData.getMaterial());
        if (seedMaterial == null) return;

        boolean seedConsumed = false;
        Iterator<Item> iterator = event.getItems().iterator();
        while (iterator.hasNext()) {
            Item itemEntity = iterator.next();
            ItemStack stack = itemEntity.getItemStack();
            if (stack.getType() == seedMaterial) {
                stack.setAmount(stack.getAmount() - 1);
                if (stack.getAmount() <= 0) {
                    itemEntity.remove();
                    iterator.remove();
                } else {
                    itemEntity.setItemStack(stack);
                }
                seedConsumed = true;
                break;
            }
        }

        if (seedConsumed) {
            Block block = event.getBlock();
            Bukkit.getScheduler().runTask(DanaTools.getInstance(), () -> {
                block.setType(originalData.getMaterial(), false);
                Ageable ageable = (Ageable) originalData;
                ageable.setAge(0);
                block.setBlockData(ageable, true);
                block.getWorld().playSound(block.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
            });
        }
    }
}
