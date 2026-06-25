package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

public class CompactorModifier extends DanaModifier {

    public CompactorModifier() {
        super("compactor");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(handItem);

        if (tool != null && tool.hasModifier("compactor")) {
            Material pickedMaterial = event.getItem().getItemStack().getType();
            
            Bukkit.getScheduler().runTaskLater(DanaTools.getInstance(), () -> {
                if (player.isOnline()) {
                    DanaTools.getInstance().getCompactorManager().tryCompact(player, pickedMaterial);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        if (tool != null && tool.hasModifier("compactor")) {
            if (event.getEnchantsToAdd().containsKey(Enchantment.SILK_TOUCH)) {
                event.getEnchantsToAdd().remove(Enchantment.SILK_TOUCH);
            }
        }
    }
}
