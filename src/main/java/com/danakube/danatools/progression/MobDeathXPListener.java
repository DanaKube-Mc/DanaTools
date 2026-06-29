package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomTool;
import com.danakube.danatools.model.DanaItemInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class MobDeathXPListener implements Listener {

    private final DanaTools plugin;

    public MobDeathXPListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        ItemStack handItem = killer.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(handItem);
        if (tool == null) {
            return;
        }

        CustomTool.MobActivity activity = tool.getConfig().getMobActivity(event.getEntityType());
        if (activity != null) {
            int xpGain = activity.getXp();
            if (xpGain > 0) {
                xpGain = plugin.getXpManager().applyLearningBoost(killer, xpGain);
                tool.addXP(xpGain, killer);
            }
            if (activity.hasCoreDrop()) {
                for (CustomTool.CoreDrop coreDrop : activity.getCoreDrops()) {
                    CoreDropManager.checkAndDropCore(killer, event.getEntity().getLocation().add(0, 0.5, 0), tool, coreDrop);
                }
            }
        }
    }
}
