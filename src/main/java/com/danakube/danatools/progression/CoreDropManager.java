package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import com.danakube.danatools.model.ToolInstance;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CoreDropManager {

    public static void checkAndDropCore(Player player, Block block, ToolInstance tool, CustomTool.BlockActivity activity) {
        if (activity == null || !activity.hasCoreDrop()) {
            return;
        }

        CustomTool.CoreDrop coreDrop = activity.getCoreDrop();
        double roll = ThreadLocalRandom.current().nextDouble(100.0);
        double chance = coreDrop.getChancePercent();

        if (roll <= chance) {
            String modifierId = coreDrop.getModifierId();
            CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier(modifierId);
            if (modifier != null) {
                ItemStack coreItem = DanaTools.getInstance().getModifierConfigManager().buildIngredientItem(modifier);
                if (coreItem != null) {
                    block.getWorld().dropItemNaturally(block.getLocation(), coreItem);
                    triggerFeedbacks(player, block);
                }
            }
        }
    }

    private static void triggerFeedbacks(Player player, Block block) {
        DanaTools plugin = DanaTools.getInstance();

        if (plugin.getConfig().getBoolean("core-drop-settings.sound.enabled", false)) {
            try {
                String typeStr = plugin.getConfig().getString("core-drop-settings.sound.type");
                if (typeStr != null) {
                    Sound sound = Sound.valueOf(typeStr.toUpperCase());
                    float volume = (float) plugin.getConfig().getDouble("core-drop-settings.sound.volume", 1.0);
                    float pitch = (float) plugin.getConfig().getDouble("core-drop-settings.sound.pitch", 1.0);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Configuration invalide pour le son de drop de noyau : " + plugin.getConfig().getString("core-drop-settings.sound.type"));
            }
        }

        if (plugin.getConfig().getBoolean("core-drop-settings.particles.enabled", false)) {
            try {
                String typeStr = plugin.getConfig().getString("core-drop-settings.particles.type");
                if (typeStr != null) {
                    Particle particle = Particle.valueOf(typeStr.toUpperCase());
                    int count = plugin.getConfig().getInt("core-drop-settings.particles.count", 15);
                    double offset = plugin.getConfig().getDouble("core-drop-settings.particles.offset", 0.3);
                    double speed = plugin.getConfig().getDouble("core-drop-settings.particles.speed", 0.1);

                    block.getWorld().spawnParticle(
                            particle,
                            block.getLocation().add(0.5, 0.5, 0.5),
                            count,
                            offset, offset, offset,
                            speed
                    );
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Configuration invalide pour la particule de drop de noyau : " + plugin.getConfig().getString("core-drop-settings.particles.type"));
            }
        }

        if (plugin.getConfig().getBoolean("core-drop-settings.action-bar.enabled", false)) {
            String messageRaw = plugin.getConfig().getString("core-drop-settings.action-bar.message");
            if (messageRaw != null && !messageRaw.isEmpty()) {
                player.sendActionBar(ToolInstance.parseColor(messageRaw));
            }
        }
    }
}
