package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import com.danakube.danatools.modifier.DropManager;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

public class AutoSmeltModifier extends DanaModifier {

    public AutoSmeltModifier() {
        super("auto_smelt");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack toolItem = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(toolItem);
        
        if (tool != null && tool.hasModifier("auto_smelt")) {
            double wisdomBoost = 0.0;
            if (tool.hasModifier("wisdom")) {
                int wisdomLvl = tool.getModifierLevel("wisdom");
                CustomModifier wisdomConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("wisdom");
                if (wisdomConfig != null) {
                    CustomModifier.LevelSettings settings = wisdomConfig.getLevel(wisdomLvl);
                    if (settings != null) {
                        Object boostObj = settings.getBehaviorSettings().get("xp-boost");
                        if (boostObj instanceof Number num) {
                            wisdomBoost = num.doubleValue();
                        }
                    }
                }
            }

            double totalXp = 0.0;
            for (Item itemEntity : event.getItems()) {
                ItemStack drop = itemEntity.getItemStack();
                DropManager.SmeltResult smelt = DropManager.getSmeltResult(drop.getType());
                if (smelt != null) {
                    totalXp += drop.getAmount() * smelt.getXp() * (1.0 + wisdomBoost);
                    ItemStack cookedDrop = new ItemStack(smelt.getResult(), drop.getAmount());
                    itemEntity.setItemStack(cookedDrop);
                }
            }
            if (totalXp > 0) {
                DropManager.spawnXP(event.getBlock().getLocation(), totalXp);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        ItemStack item = killer.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        
        if (tool != null && tool.hasModifier("auto_smelt")) {
            double wisdomBoost = 0.0;
            if (tool.hasModifier("wisdom")) {
                int wisdomLvl = tool.getModifierLevel("wisdom");
                CustomModifier wisdomConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("wisdom");
                if (wisdomConfig != null) {
                    CustomModifier.LevelSettings settings = wisdomConfig.getLevel(wisdomLvl);
                    if (settings != null) {
                        Object boostObj = settings.getBehaviorSettings().get("xp-boost");
                        if (boostObj instanceof Number num) {
                            wisdomBoost = num.doubleValue();
                        }
                    }
                }
            }

            double totalXp = 0.0;
            java.util.List<ItemStack> drops = event.getDrops();
            for (int i = 0; i < drops.size(); i++) {
                ItemStack drop = drops.get(i);
                DropManager.SmeltResult smelt = DropManager.getSmeltResult(drop.getType());
                if (smelt != null) {
                    totalXp += drop.getAmount() * smelt.getXp() * (1.0 + wisdomBoost);
                    drops.set(i, new ItemStack(smelt.getResult(), drop.getAmount()));
                }
            }
            if (totalXp > 0) {
                DropManager.spawnXP(event.getEntity().getLocation(), totalXp);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        
        if (tool != null && tool.hasModifier("auto_smelt")) {
            ItemStack fishStack = caughtItem.getItemStack();
            DropManager.SmeltResult smelt = DropManager.getSmeltResult(fishStack.getType());
            
            if (smelt != null) {
                ItemStack cookedFish = new ItemStack(smelt.getResult(), fishStack.getAmount());
                caughtItem.setItemStack(cookedFish);

                double wisdomBoost = 0.0;
                if (tool.hasModifier("wisdom")) {
                    int wisdomLvl = tool.getModifierLevel("wisdom");
                    CustomModifier wisdomConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("wisdom");
                    if (wisdomConfig != null) {
                        CustomModifier.LevelSettings settings = wisdomConfig.getLevel(wisdomLvl);
                        if (settings != null) {
                            Object boostObj = settings.getBehaviorSettings().get("xp-boost");
                            if (boostObj instanceof Number num) {
                                wisdomBoost = num.doubleValue();
                            }
                        }
                    }
                }

                double addedExp = fishStack.getAmount() * smelt.getXp() * (1.0 + wisdomBoost);
                event.setExpToDrop(event.getExpToDrop() + (int) Math.round(addedExp));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        if (tool != null && tool.hasModifier("auto_smelt")) {
            if (event.getEnchantsToAdd().containsKey(Enchantment.SILK_TOUCH)) {
                event.getEnchantsToAdd().remove(Enchantment.SILK_TOUCH);
            }
        }
    }

}
