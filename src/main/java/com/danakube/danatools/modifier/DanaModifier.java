package com.danakube.danatools.modifier;

import com.danakube.danatools.model.DanaItemInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class DanaModifier implements Listener {
    public static final ThreadLocal<Boolean> processingCustomBreak = ThreadLocal.withInitial(() -> false);
    private final String id;

    public DanaModifier(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    protected boolean isEquipped(Player player) {
        return getHighestModifierLevel(player) > 0;
    }

    protected int getHighestModifierLevel(Player player) {
        return getHighestModifierLevel(player, this.id);
    }

    public static int getHighestModifierLevel(Player player, String modifierId) {
        if (player == null) return 0;
        int maxLvl = 0;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        DanaItemInstance mainHandTool = DanaItemInstance.fromItemStack(mainHand);
        if (mainHandTool != null && mainHandTool.hasModifier(modifierId)) {
            maxLvl = Math.max(maxLvl, mainHandTool.getModifierLevel(modifierId));
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        DanaItemInstance offHandTool = DanaItemInstance.fromItemStack(offHand);
        if (offHandTool != null && offHandTool.hasModifier(modifierId)) {
            maxLvl = Math.max(maxLvl, offHandTool.getModifierLevel(modifierId));
        }

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null) {
                DanaItemInstance armorInstance = DanaItemInstance.fromItemStack(armorPiece);
                if (armorInstance != null && armorInstance.hasModifier(modifierId)) {
                    maxLvl = Math.max(maxLvl, armorInstance.getModifierLevel(modifierId));
                }
            }
        }

        return maxLvl;
    }

    public static ItemStack getHighestModifierItem(Player player, String modifierId) {
        if (player == null) return null;
        int maxLvl = 0;
        ItemStack bestItem = null;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        DanaItemInstance mainHandTool = DanaItemInstance.fromItemStack(mainHand);
        if (mainHandTool != null && mainHandTool.hasModifier(modifierId)) {
            maxLvl = mainHandTool.getModifierLevel(modifierId);
            bestItem = mainHand;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        DanaItemInstance offHandTool = DanaItemInstance.fromItemStack(offHand);
        if (offHandTool != null && offHandTool.hasModifier(modifierId)) {
            int lvl = offHandTool.getModifierLevel(modifierId);
            if (lvl > maxLvl) {
                maxLvl = lvl;
                bestItem = offHand;
            }
        }

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null) {
                DanaItemInstance armorInstance = DanaItemInstance.fromItemStack(armorPiece);
                if (armorInstance != null && armorInstance.hasModifier(modifierId)) {
                    int lvl = armorInstance.getModifierLevel(modifierId);
                    if (lvl > maxLvl) {
                        maxLvl = lvl;
                        bestItem = armorPiece;
                    }
                }
            }
        }

        return bestItem;
    }
}
