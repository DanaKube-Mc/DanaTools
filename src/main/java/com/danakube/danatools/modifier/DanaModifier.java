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
        if (player == null) return false;
        ItemStack item = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(item);
        if (tool == null) return false;

        return tool.hasModifier(this.id);
    }
}
