package com.danakube.danatools.integration;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.danakube.danatools.DanaTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WildStackerHook {

    private final boolean enabled;

    public WildStackerHook() {
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("WildStacker");
        if (this.enabled) {
            DanaTools.getInstance().getLogger().info("Liaison avec WildStacker activee avec succes !");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Retourne le nombre total reel d'items pour cette entite.
     * Si WildStacker est present, retourne la quantite du stack, sinon la quantite du ItemStack vanilla.
     */
    public int getItemAmount(Item item) {
        if (item == null) {
            return 0;
        }
        if (enabled) {
            try {
                return WildStackerAPI.getItemAmount(item);
            } catch (Throwable ignored) {
            }
        }
        return item.getItemStack().getAmount();
    }

    /**
     * Retire une quantite donnee du stack d'items.
     * Si la quantite a retirer est superieure ou egale au montant total, supprime l'entite.
     * Sinon, diminue le stack sans supprimer l'entite du monde.
     */
    public void decreaseItemAmount(Item item, int amountToTake) {
        if (item == null || amountToTake <= 0) {
            return;
        }
        if (enabled) {
            try {
                StackedItem stackedItem = WildStackerAPI.getStackedItem(item);
                if (stackedItem != null) {
                    int current = stackedItem.getStackAmount();
                    if (amountToTake >= current) {
                        stackedItem.remove();
                    } else {
                        stackedItem.decreaseStackAmount(amountToTake, true);
                    }
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        // Fallback Vanilla
        int current = item.getItemStack().getAmount();
        if (amountToTake >= current) {
            item.remove();
        } else {
            org.bukkit.inventory.ItemStack stack = item.getItemStack();
            stack.setAmount(current - amountToTake);
            item.setItemStack(stack);
        }
    }

    /**
     * Calcule combien d'exemplaires de cet ItemStack peuvent encore entrer dans l'inventaire du joueur.
     */
    public static int getAvailableSpace(Player player, ItemStack sample) {
        if (player == null || sample == null || sample.getType().isAir()) {
            return 0;
        }
        int available = 0;
        int maxStack = sample.getMaxStackSize();
        org.bukkit.inventory.ItemStack[] storage = player.getInventory().getStorageContents();
        for (org.bukkit.inventory.ItemStack current : storage) {
            if (current == null || current.getType().isAir()) {
                available += maxStack;
            } else if (current.isSimilar(sample)) {
                available += Math.max(0, maxStack - current.getAmount());
            }
        }
        return available;
    }

    /**
     * Tente de ramasser des items depuis une entite Item (compatible WildStacker et Vanilla).
     * @return le nombre d'items effectivement donnes au joueur.
     */
    public int pickupItem(Player player, Item item) {
        if (player == null || item == null || item.isDead() || !item.isValid()) {
            return 0;
        }

        org.bukkit.inventory.ItemStack sample = item.getItemStack();
        int space = getAvailableSpace(player, sample);
        if (space <= 0) {
            return 0;
        }

        int totalInEntity = getItemAmount(item);
        if (totalInEntity <= 0) {
            return 0;
        }

        int toGive = Math.min(totalInEntity, space);
        if (toGive <= 0) {
            return 0;
        }

        // Donner au joueur par paquets <= maxStackSize
        int remainingToGive = toGive;
        int maxStack = sample.getMaxStackSize();
        while (remainingToGive > 0) {
            int batchSize = Math.min(remainingToGive, maxStack);
            org.bukkit.inventory.ItemStack batch = sample.clone();
            batch.setAmount(batchSize);
            player.getInventory().addItem(batch);
            remainingToGive -= batchSize;
        }

        // Décrémenter l'entité (WildStacker ou vanilla)
        decreaseItemAmount(item, toGive);
        return toGive;
    }
}
