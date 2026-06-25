package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AutoSellManager {

    private final DanaTools plugin;
    private final Map<Material, Double> prices = new HashMap<>();
    private final Map<UUID, Double> tickEarnings = new HashMap<>();

    public AutoSellManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void loadPrices() {
        prices.clear();
        File file = new File(plugin.getDataFolder(), "modifiers/auto_sell.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("Fichier de configuration modifiers/auto_sell.yml introuvable.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection pricesSection = config.getConfigurationSection("prices");
        if (pricesSection != null) {
            for (String key : pricesSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    double price = pricesSection.getDouble(key);
                    prices.put(mat, price);
                } else {
                    plugin.getLogger().warning("Materiau invalide ou inconnu dans auto_sell.yml : " + key);
                }
            }
        }
        plugin.getLogger().info("Charge " + prices.size() + " prix de vente pour l'auto-vente.");
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }

    public boolean sellItem(Player player, ItemStack item, double multiplier) {
        Economy econ = plugin.getEconomy();
        if (econ == null) {
            return false;
        }

        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return false;
        }

        double price = getPrice(item.getType());
        if (price <= 0) {
            return false;
        }

        double totalValue = price * item.getAmount() * multiplier;
        if (totalValue <= 0) {
            return false;
        }

        econ.depositPlayer(player, totalValue);
        addEarning(player, totalValue);
        return true;
    }

    private void addEarning(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = tickEarnings.getOrDefault(uuid, 0.0);
        tickEarnings.put(uuid, current + amount);

        if (current == 0.0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Double total = tickEarnings.remove(uuid);
                if (total != null && total > 0) {
                    Component msg = plugin.getLangManager().getMessage(
                            "auto_sell.action_bar",
                            "{amount}", String.format(Locale.US, "%.2f", total)
                    );
                    player.sendActionBar(msg);
                }
            }, 1L);
        }
    }
}
