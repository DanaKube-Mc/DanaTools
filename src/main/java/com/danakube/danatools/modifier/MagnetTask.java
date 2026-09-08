package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
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

            int level = DanaModifier.getHighestModifierLevel(player, "magnet");
            if (level > 0) {
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
                if (itemEntity.isDead() || !itemEntity.isValid() || itemEntity.getPickupDelay() > 0) {
                    continue;
                }

                EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent(player, itemEntity, 0);
                Bukkit.getPluginManager().callEvent(pickupEvent);
                if (pickupEvent.isCancelled()) {
                    continue;
                }

                int picked = DanaTools.getInstance().getWildStackerHook().pickupItem(player, itemEntity);
                if (picked > 0) {
                    itemPickedUp = true;
                }
            }
        }

        if (itemPickedUp) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
        }
    }
}
