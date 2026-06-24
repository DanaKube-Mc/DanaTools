package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.ToolInstance;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class MagnetTask implements Runnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            ToolInstance tool = ToolInstance.fromItemStack(hand);

            if (tool != null && tool.hasModifier("magnet")) {
                int level = tool.getModifierLevel("magnet");
                double radius = getDetectionRadius(level);

                if (radius > 0) {
                    vacuumItems(player, radius);
                }
            }
        }
    }

    private double getDetectionRadius(int level) {
        CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier("magnet");
        if (modifier == null) return 0;
        CustomModifier.LevelSettings settings = modifier.getLevel(level);
        if (settings == null) return 0;

        return settings.getBehaviorDouble("radius", 4.0);
    }

    private void vacuumItems(Player player, double radius) {
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        boolean itemPickedUp = false;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Item itemEntity) {
                if (itemEntity.getPickupDelay() > 0) {
                    continue;
                }

                ItemStack itemStack = itemEntity.getItemStack();
                int originalAmount = itemStack.getAmount();

                EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent(player, itemEntity, 0);
                Bukkit.getPluginManager().callEvent(pickupEvent);
                if (pickupEvent.isCancelled()) {
                    continue;
                }

                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(itemStack);

                if (remaining.isEmpty()) {
                    itemEntity.remove();
                    itemPickedUp = true;
                } else {
                    ItemStack leftOver = remaining.get(0);
                    if (leftOver.getAmount() < originalAmount) {
                        itemEntity.setItemStack(leftOver);
                        itemPickedUp = true;
                    }
                }
            }
        }

        if (itemPickedUp) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
        }
    }
}
