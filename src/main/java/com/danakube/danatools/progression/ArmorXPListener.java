package com.danakube.danatools.progression;

import com.danakube.danatools.model.DanaItemInstance;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorXPListener implements Listener {

    private final ArmorExplorationTask explorationTask;

    public ArmorXPListener(ArmorExplorationTask explorationTask) {
        this.explorationTask = explorationTask;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double damage = event.getFinalDamage();
        if (damage <= 0) {
            return;
        }

        Entity damager = event.getDamager();
        if (damager == player) {
            return;
        }

        if (damager instanceof Projectile proj) {
            if (proj.getShooter() == player) {
                return;
            }
        }
        if (damager instanceof ThrownPotion potion) {
            if (potion.getShooter() == player) {
                return;
            }
        }
        if (damager instanceof AreaEffectCloud cloud) {
            if (cloud.getSource() == player) {
                return;
            }
        }

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece != null) {
                DanaItemInstance itemInstance = DanaItemInstance.fromItemStack(piece);
                if (itemInstance != null) {
                    double mult = itemInstance.getConfig().getXpGainDamageMultiplier();
                    if (mult > 0) {
                        int xp = (int) Math.round(damage * mult);
                        if (xp > 0) {
                            itemInstance.addXP(xp, player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        explorationTask.cleanupPlayer(event.getPlayer().getUniqueId());
    }
}
