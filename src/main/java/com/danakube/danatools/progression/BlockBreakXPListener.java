package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.ToolInstance;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakXPListener implements Listener {

    private final DanaTools plugin;

    public BlockBreakXPListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        ToolInstance tool = ToolInstance.fromItemStack(handItem);
        if (tool == null) {
            return;
        }

        Material blockType = event.getBlock().getType();
        CustomTool.BlockActivity activity = tool.getConfig().getBlockActivity(blockType);

        if (activity != null) {
            int xpGain = activity.getXp();
            if (xpGain > 0) {
                if (tool.hasModifier("learning")) {
                    int learningLvl = tool.getModifierLevel("learning");
                    CustomModifier learningConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("learning");
                    if (learningConfig != null) {
                        CustomModifier.LevelSettings settings = learningConfig.getLevel(learningLvl);
                        if (settings != null) {
                            Object boostObj = settings.getBehaviorSettings().get("xp-boost");
                            if (boostObj instanceof Number num) {
                                xpGain = (int) Math.round(xpGain * (1.0 + num.doubleValue()));
                            }
                        }
                    }
                }
                tool.addXP(xpGain, player);
            }
            CoreDropManager.checkAndDropCore(player, event.getBlock(), tool, activity);
        }
    }
}
