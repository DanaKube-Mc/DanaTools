package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;

import java.util.List;

import org.bukkit.GameEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.inventory.ItemStack;

public class FeatherStepModifier extends DanaModifier {

    public FeatherStepModifier() {
        super("feather_step");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) {
            return;
        }

        DanaItemInstance bootsInstance = DanaItemInstance.fromItemStack(boots);
        if (bootsInstance == null || !bootsInstance.hasModifier("feather_step")) {
            return;
        }

        int level = bootsInstance.getModifierLevel("feather_step");
        if (level <= 0) {
            return;
        }

        org.bukkit.block.Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Material mat = block.getType();
        boolean cancel = false;

        CustomModifier modifierConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("feather_step");
        if (modifierConfig != null) {
            for (int lvl = 1; lvl <= level; lvl++) {
                CustomModifier.LevelSettings settings = modifierConfig.getLevel(lvl);
                if (settings != null) {
                    Object matsObj = settings.getBehaviorSettings().get("materials");
                    if (matsObj instanceof List<?> list) {
                        for (Object item : list) {
                            if (item != null && mat.name().equalsIgnoreCase(item.toString())) {
                                cancel = true;
                                break;
                            }
                        }
                    }
                    Object subObj = settings.getBehaviorSettings().get("material-substrings");
                    if (subObj instanceof List<?> list) {
                        for (Object item : list) {
                            if (item != null && mat.name().toUpperCase().contains(item.toString().toUpperCase())) {
                                cancel = true;
                                break;
                            }
                        }
                    }
                }
                if (cancel) {
                    break;
                }
            }
        } else {
            if (level >= 1) {
                if (mat == Material.FARMLAND || mat == Material.TURTLE_EGG || mat == Material.BIG_DRIPLEAF) {
                    cancel = true;
                }
            }
            if (level >= 2 && !cancel) {
                if (mat == Material.TRIPWIRE || mat.name().contains("PRESSURE_PLATE")) {
                    cancel = true;
                }
            }
        }

        if (cancel) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGenericGameEvent(GenericGameEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) {
            return;
        }

        DanaItemInstance bootsInstance = DanaItemInstance.fromItemStack(boots);
        if (bootsInstance == null || !bootsInstance.hasModifier("feather_step")) {
            return;
        }

        int level = bootsInstance.getModifierLevel("feather_step");
        if (level <= 0) {
            return;
        }

        GameEvent type = event.getEvent();
        String eventKey = type.getKey().getKey();
        boolean cancel = false;

        CustomModifier modifierConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("feather_step");
        if (modifierConfig != null) {
            for (int lvl = 1; lvl <= level; lvl++) {
                CustomModifier.LevelSettings settings = modifierConfig.getLevel(lvl);
                if (settings != null) {
                    Object eventsObj = settings.getBehaviorSettings().get("events");
                    if (eventsObj instanceof List<?> list) {
                        for (Object item : list) {
                            if (item != null) {
                                String configEvent = item.toString().toLowerCase();
                                if (configEvent.startsWith("minecraft:")) {
                                    configEvent = configEvent.substring("minecraft:".length());
                                }
                                if (eventKey.equalsIgnoreCase(configEvent) || eventKey.replace("_", "").equalsIgnoreCase(configEvent.replace("_", ""))) {
                                    cancel = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (cancel) {
                    break;
                }
            }
        } else {
            if (level >= 3) {
                cancel = (type == GameEvent.STEP || type == GameEvent.HIT_GROUND || type == GameEvent.SWIM || type == GameEvent.SPLASH);
            }
        }


        if (cancel) {
            event.setCancelled(true);
        }
    }
}

