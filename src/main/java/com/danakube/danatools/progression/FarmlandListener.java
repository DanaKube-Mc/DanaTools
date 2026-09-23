package com.danakube.danatools.progression;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class FarmlandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND && event.getNewState().getType() == Material.DIRT) {
            // Empêche la terre labourée de se dessécher et de redevenir de la terre
            event.setCancelled(true);
        }
    }
}
