package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class WisdomModifier extends DanaModifier {

    public WisdomModifier() {
        super("wisdom");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        
        if (tool != null && tool.hasModifier("wisdom")) {
            int level = tool.getModifierLevel("wisdom");
            double boost = getXpBoost("wisdom", level);
            
            int originalExp = event.getExpToDrop();
            if (originalExp > 0) {
                int newExp = (int) Math.round(originalExp * (1.0 + boost));
                event.setExpToDrop(newExp);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        ItemStack item = killer.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        
        if (tool != null && tool.hasModifier("wisdom")) {
            int level = tool.getModifierLevel("wisdom");
            double boost = getXpBoost("wisdom", level);
            
            int originalExp = event.getDroppedExp();
            if (originalExp > 0) {
                int newExp = (int) Math.round(originalExp * (1.0 + boost));
                event.setDroppedExp(newExp);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        
        if (tool != null && tool.hasModifier("wisdom")) {
            int level = tool.getModifierLevel("wisdom");
            double boost = getXpBoost("wisdom", level);
            
            int originalExp = event.getExpToDrop();
            if (originalExp > 0) {
                int newExp = (int) Math.round(originalExp * (1.0 + boost));
                event.setExpToDrop(newExp);
            }
        }
    }

    private double getXpBoost(String modifierId, int level) {
        CustomModifier config = DanaTools.getInstance().getModifierConfigManager().getModifier(modifierId);
        if (config != null) {
            CustomModifier.LevelSettings settings = config.getLevel(level);
            if (settings != null) {
                Object obj = settings.getBehaviorSettings().get("xp-boost");
                if (obj instanceof Number num) {
                    return num.doubleValue();
                }
            }
        }
        return 0.0;
    }
}
