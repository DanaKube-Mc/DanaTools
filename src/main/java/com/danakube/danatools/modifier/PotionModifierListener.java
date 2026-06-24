package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class PotionModifierListener implements Listener {

    private final DanaTools plugin;

    public PotionModifierListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        plugin.getPotionModifierManager().removeAllPluginEffects(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPotionModifierManager().checkAndApply(player);
        }, 1L);
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        plugin.getPotionModifierManager().removeAllPluginEffects(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPotionModifierManager().checkAndApply(player);
        }, 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        plugin.getPotionModifierManager().removeAllPluginEffects(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPotionModifierManager().checkAndApply(player);
        }, 1L);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getPotionModifierManager().removeAllPluginEffects(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPotionModifierManager().checkAndApply(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPotionModifierManager().removeAllPluginEffects(event.getPlayer());
    }
}
