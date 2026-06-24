package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.ToolInstance;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class PotionModifierManager {

    private static final String POTION_METADATA_PREFIX = "danatools_potion_";
    private final DanaTools plugin;

    public PotionModifierManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void checkAndApply(Player player) {
        if (player == null || !player.isOnline()) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        ToolInstance tool = ToolInstance.fromItemStack(hand);

        if (tool != null) {
            for (String modId : tool.getModifiers()) {
                CustomModifier modifier = plugin.getModifierConfigManager().getModifier(modId);
                if (modifier == null) continue;

                int activeLvl = tool.getModifierLevel(modId);
                CustomModifier.LevelSettings settings = modifier.getLevel(activeLvl);

                if (settings != null && "POTION_EFFECT".equalsIgnoreCase(settings.getBehaviorType())) {
                    applyPotionEffect(player, modId, settings);
                }
            }
        }
    }

    private void applyPotionEffect(Player player, String modifierId, CustomModifier.LevelSettings settings) {
        Object effectNameObj = settings.getBehaviorSettings().get("effect-type");
        if (effectNameObj == null) return;

        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectNameObj.toString().toLowerCase(Locale.ROOT)));
        if (type == null) return;

        int amplifier = settings.getBehaviorInt("amplifier", 0);
        boolean ambient = true;
        Object ambientObj = settings.getBehaviorSettings().get("ambient");
        if (ambientObj instanceof Boolean) {
            ambient = (Boolean) ambientObj;
        }

        boolean particles = false;
        Object particlesObj = settings.getBehaviorSettings().get("particles");
        if (particlesObj instanceof Boolean) {
            particles = (Boolean) particlesObj;
        }

        PotionEffect effect = new PotionEffect(type, 100, amplifier, ambient, particles, true);
        player.addPotionEffect(effect);

        player.setMetadata(POTION_METADATA_PREFIX + modifierId, new FixedMetadataValue(plugin, true));
    }

    public void removeAllPluginEffects(Player player) {
        if (player == null) return;

        for (CustomModifier modifier : plugin.getModifierConfigManager().getModifiers()) {
            CustomModifier.LevelSettings settings = modifier.getLevel(1);
            if (settings != null && "POTION_EFFECT".equalsIgnoreCase(settings.getBehaviorType())) {
                String key = POTION_METADATA_PREFIX + modifier.getId();
                if (player.hasMetadata(key)) {
                    Object effectNameObj = settings.getBehaviorSettings().get("effect-type");
                    if (effectNameObj != null) {
                        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectNameObj.toString().toLowerCase(Locale.ROOT)));
                        if (type != null) {
                            player.removePotionEffect(type);
                        }
                    }
                    player.removeMetadata(key, plugin);
                }
            }
        }
    }
}
