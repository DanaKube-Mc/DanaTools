package com.danakube.danatools.integration;

import com.danakube.danatools.DanaTools;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EconomyShopHook {

    private final boolean enabled;

    public EconomyShopHook() {
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("EconomyShopGUI")
                || Bukkit.getPluginManager().isPluginEnabled("EconomyShopGUI-Premium");
        if (this.enabled) {
            DanaTools.getInstance().getLogger().info("Liaison avec EconomyShopGUI / EconomyShopGUI-Premium activee avec succes !");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ShopItem getShopItem(ItemStack item) {
        return getShopItem(null, item);
    }

    public ShopItem getShopItem(Player player, ItemStack item) {
        if (!enabled || item == null) {
            return null;
        }
        try {
            if (player != null) {
                ShopItem shopItem = EconomyShopGUIHook.getShopItem(player, item);
                if (shopItem != null) {
                    return shopItem;
                }
            }
            return EconomyShopGUIHook.getShopItem(item);
        } catch (Throwable t) {
            return null;
        }
    }

    public boolean isSellable(ShopItem shopItem) {
        if (!enabled || shopItem == null) {
            return false;
        }
        try {
            return EconomyShopGUIHook.isSellAble(shopItem);
        } catch (Throwable t) {
            return false;
        }
    }

    public double getSellPrice(ShopItem shopItem, ItemStack item, Player player) {
        if (!enabled || shopItem == null || item == null) {
            return 0.0;
        }
        try {
            Double price = null;
            if (shopItem.isDynamicPricing()) {
                price = EconomyShopGUIHook.getItemSellPrice(shopItem, item, player, item.getAmount(), 0);
            }
            if (price == null || price <= 0) {
                price = EconomyShopGUIHook.getItemSellPrice(shopItem, item, player);
            }
            if (price == null || price <= 0) {
                price = EconomyShopGUIHook.getItemSellPrice(shopItem, item);
            }
            return price != null ? price : 0.0;
        } catch (Throwable t) {
            return 0.0;
        }
    }

    public void processSale(ShopItem shopItem, Player player, int amount) {
        if (!enabled || shopItem == null) {
            return;
        }
        try {
            if (shopItem.isRefillStock()) {
                EconomyShopGUIHook.sellItemStock(shopItem, player.getUniqueId(), amount);
            }
            if (shopItem.getLimitedSellMode() != 0) {
                EconomyShopGUIHook.sellItemLimit(shopItem, player.getUniqueId(), amount);
            }
            if (shopItem.isDynamicPricing()) {
                EconomyShopGUIHook.sellItem(shopItem, amount);
            }
        } catch (Throwable t) {
            DanaTools.getInstance().getLogger().warning("Erreur lors de l'enregistrement de la vente dans EconomyShopGUI : " + t.getMessage());
        }
    }
}
