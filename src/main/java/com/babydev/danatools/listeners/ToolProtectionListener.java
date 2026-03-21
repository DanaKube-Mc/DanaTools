package com.babydev.danatools.listeners;

import com.babydev.danatools.DanaTools;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;
import java.util.List;

public class ToolProtectionListener implements Listener {

    private final DanaTools plugin;

    public ToolProtectionListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        List<ItemStack> drops = event.getDrops();
        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            if (isEvolutionTool(drop)) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (isEvolutionTool(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("cannot_drop_tool"));
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (isEvolutionTool(clickedItem)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                return;
            }

            if (event.isShiftClick() && event.getInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                return;
            }
        }

        if (isEvolutionTool(cursorItem)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isEvolutionTool(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;
        return meta.getPersistentDataContainer().has(DanaTools.TOOL_ID_KEY, PersistentDataType.STRING);
    }
}
