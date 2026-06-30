package com.danakube.danatools.forge;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;


public class SmithingListener implements Listener {

    private final DanaTools plugin;

    public SmithingListener(DanaTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        SmithingInventory inv = event.getInventory();
        ItemStack template = inv.getItem(0);
        ItemStack base = inv.getItem(1);
        ItemStack addition = inv.getItem(2);

        if (template == null || base == null || addition == null) {
            return;
        }

        DanaItemInstance tool = DanaItemInstance.fromItemStack(base);
        if (tool == null) {
            return;
        }

        for (CustomModifier modifier : plugin.getModifierConfigManager().getModifiers()) {
            if (matchesTemplate(template, modifier)) {
                if (matchesIngredient(addition, modifier)) {
                    if (tool.canApplyOrUpgradeModifier(modifier)) {
                        ItemStack result = base.clone();
                        DanaItemInstance resultTool = DanaItemInstance.fromItemStack(result);
                        if (resultTool != null) {
                            resultTool.applyOrUpgradeModifier(modifier);
                            event.setResult(resultTool.getItemStack());
                            return;
                        }
                    }
                }
                event.setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onSmithItem(SmithItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result == null) return;

        DanaItemInstance tool = DanaItemInstance.fromItemStack(result);
        if (tool == null) return;

        if (event.getWhoClicked() instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
            
            Block block = event.getInventory().getLocation() != null ? event.getInventory().getLocation().getBlock() : null;
            if (block != null) {
                player.spawnParticle(Particle.LAVA, block.getLocation().add(0.5, 1.0, 0.5), 10, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    private boolean matchesTemplate(ItemStack item, CustomModifier modifier) {
        if (item == null || item.getType() != modifier.getTemplateMaterial()) {
            return false;
        }
        if (modifier.getTemplateCustomModelData() > 0) {
            if (!item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasCustomModelData() || meta.getCustomModelData() != modifier.getTemplateCustomModelData()) {
                return false;
            }
        }
        if (modifier.getTemplateDisplayName() != null && !modifier.getTemplateDisplayName().isEmpty()) {
            if (!item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasDisplayName()) return false;
            Component expected = DanaItemInstance.parseColor(modifier.getTemplateDisplayName());
            Component actual = meta.displayName();
            if (actual == null) return false;
            if (!actual.equals(expected)) {
                String plainExpected = PlainTextComponentSerializer.plainText().serialize(expected);
                String plainActual = PlainTextComponentSerializer.plainText().serialize(actual);
                if (!plainExpected.equalsIgnoreCase(plainActual)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesIngredient(ItemStack item, CustomModifier modifier) {
        if (item == null || item.getType() != modifier.getIngredientMaterial()) {
            return false;
        }

        if (modifier.getIngredientMaterial() == Material.PLAYER_HEAD && modifier.getIngredientTexture() != null && !modifier.getIngredientTexture().isEmpty()) {
            if (item.hasItemMeta() && item.getItemMeta() instanceof SkullMeta skullMeta) {
                PlayerProfile profile = skullMeta.getPlayerProfile();
                if (profile != null) {
                    for (ProfileProperty prop : profile.getProperties()) {
                        if (prop.getName().equals("textures")) {
                            return prop.getValue().equals(modifier.getIngredientTexture());
                        }
                    }
                }
            }
            return false;
        }

        if (modifier.getIngredientCustomModelData() > 0) {
            if (!item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasCustomModelData() || meta.getCustomModelData() != modifier.getIngredientCustomModelData()) {
                return false;
            }
        }

        if (modifier.getIngredientDisplayName() != null && !modifier.getIngredientDisplayName().isEmpty()) {
            if (!item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasDisplayName()) return false;
            Component expected = DanaItemInstance.parseColor(modifier.getIngredientDisplayName());
            Component actual = meta.displayName();
            if (actual == null) return false;
            if (!actual.equals(expected)) {
                String plainExpected = PlainTextComponentSerializer.plainText().serialize(expected);
                String plainActual = PlainTextComponentSerializer.plainText().serialize(actual);
                if (!plainExpected.equalsIgnoreCase(plainActual)) {
                    return false;
                }
            }
        }

        return true;
    }
}
