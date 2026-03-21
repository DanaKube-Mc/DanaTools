package com.babydev.danatools.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ToolDetailMenuHolder implements InventoryHolder {

    private final String toolId;

    public ToolDetailMenuHolder(String toolId) {
        this.toolId = toolId;
    }

    public String getToolId() {
        return toolId;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
