package com.danakube.danatools.progression;

import com.danakube.danatools.model.DanaItemInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorExplorationTask implements Runnable {

    private final Map<UUID, Location> lastPositions = new HashMap<>();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isInsideVehicle() || player.isGliding()) {
                lastPositions.remove(player.getUniqueId());
                continue;
            }

            Location current = player.getLocation();
            Location prev = lastPositions.put(player.getUniqueId(), current);

            if (prev != null && prev.getWorld() == current.getWorld()) {
                double distance = current.distance(prev);
                if (distance > 2.0 && distance < 50.0) {
                    ItemStack[] armor = player.getInventory().getArmorContents();
                    for (ItemStack piece : armor) {
                        if (piece != null) {
                            DanaItemInstance itemInstance = DanaItemInstance.fromItemStack(piece);
                            if (itemInstance != null) {
                                double mult = itemInstance.getConfig().getXpGainMovementMultiplier();
                                if (mult > 0) {
                                    int xp = (int) Math.round(distance * mult);
                                    if (xp > 0) {
                                        itemInstance.addXP(xp, player);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void cleanupPlayer(UUID uuid) {
        lastPositions.remove(uuid);
    }
}
