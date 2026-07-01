package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import com.danakube.danatools.modifier.AutoSellManager;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class AutoSellModifier extends DanaModifier {

    public AutoSellModifier() {
        super("auto_sell");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack toolItem = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(toolItem);

        if (tool != null && tool.hasBehavior("AUTO_SELL")) {
            int level = tool.getBehaviorLevel("AUTO_SELL");
            CustomModifier config = tool.getBehaviorModifier("AUTO_SELL");
            if (config != null) {
                CustomModifier.LevelSettings settings = config.getLevel(level);
                if (settings != null) {
                    double multiplier = settings.getBehaviorDouble("multiplier", 1.0);
                    AutoSellManager asm = DanaTools.getInstance().getAutoSellManager();

                    Iterator<Item> iterator = event.getItems().iterator();
                    while (iterator.hasNext()) {
                        Item itemEntity = iterator.next();
                        ItemStack drop = itemEntity.getItemStack();
                        if (asm.sellItem(player, drop, multiplier)) {
                            itemEntity.remove();
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack toolItem = killer.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(toolItem);

        if (tool != null && tool.hasBehavior("AUTO_SELL")) {
            int level = tool.getBehaviorLevel("AUTO_SELL");
            CustomModifier config = tool.getBehaviorModifier("AUTO_SELL");
            if (config != null) {
                CustomModifier.LevelSettings settings = config.getLevel(level);
                if (settings != null) {
                    double multiplier = settings.getBehaviorDouble("multiplier", 1.0);
                    AutoSellManager asm = DanaTools.getInstance().getAutoSellManager();

                    List<ItemStack> drops = event.getDrops();
                    Iterator<ItemStack> iterator = drops.iterator();
                    while (iterator.hasNext()) {
                        ItemStack drop = iterator.next();
                        if (asm.sellItem(killer, drop, multiplier)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        Player player = event.getPlayer();
        ItemStack toolItem = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(toolItem);

        if (tool != null && tool.hasBehavior("AUTO_SELL")) {
            int level = tool.getBehaviorLevel("AUTO_SELL");
            CustomModifier config = tool.getBehaviorModifier("AUTO_SELL");
            if (config != null) {
                CustomModifier.LevelSettings settings = config.getLevel(level);
                if (settings != null) {
                    double multiplier = settings.getBehaviorDouble("multiplier", 1.0);
                    AutoSellManager asm = DanaTools.getInstance().getAutoSellManager();

                    ItemStack fishStack = caughtItem.getItemStack();
                    if (asm.sellItem(player, fishStack, multiplier)) {
                        caughtItem.remove();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        if (tool != null && tool.hasBehavior("AUTO_SELL")) {
            if (event.getEnchantsToAdd().containsKey(Enchantment.SILK_TOUCH)) {
                event.getEnchantsToAdd().remove(Enchantment.SILK_TOUCH);
            }
        }
    }
}
