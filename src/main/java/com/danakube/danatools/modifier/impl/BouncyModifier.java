package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.modifier.DanaModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class BouncyModifier extends DanaModifier {

    public BouncyModifier() {
        super("bouncy");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getCause() != DamageCause.FALL) {
            return;
        }

        if (!isEquipped(player)) {
            return;
        }

        float fallDistance = player.getFallDistance();
        if (fallDistance <= 0f) {
            return;
        }

        int level = getHighestModifierLevel(player);
        if (level <= 0) {
            return;
        }

        CustomModifier modifierConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("bouncy");
        if (modifierConfig == null) {
            return;
        }

        CustomModifier.LevelSettings settings = modifierConfig.getLevel(level);
        if (settings == null) {
            return;
        }

        double bounceFactor = settings.getBehaviorDouble("bounce-factor", 0.3);
        double maxVelocity = settings.getBehaviorDouble("max-velocity", 1.2);

        event.setCancelled(true);

        double calculatedVelocity = 0.4 * Math.sqrt(fallDistance * bounceFactor);
        double finalVelocityY = Math.min(calculatedVelocity, maxVelocity);

        player.setFallDistance(0f);

        Bukkit.getScheduler().runTaskLater(DanaTools.getInstance(), () -> {
            if (player.isOnline()) {
                player.setVelocity(new Vector(0, finalVelocityY, 0));
                player.setFallDistance(0f);
            }
        }, 1L);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_FALL, 1.0f, 1.0f);
        player.getWorld().spawnParticle(
                Particle.BLOCK,
                player.getLocation(),
                15,
                0.5,
                0.1,
                0.5,
                0.1,
                Material.SLIME_BLOCK.createBlockData()
        );
    }
}
