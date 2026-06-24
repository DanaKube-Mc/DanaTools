package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.ToolInstance;
import com.danakube.danatools.modifier.DanaModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class ChainStripperModifier extends DanaModifier {

    public ChainStripperModifier() {
        super("chain_stripper");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!isEquipped(player)) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        ItemStack axe = event.getItem();
        if (axe == null) return;

        ToolInstance tool = ToolInstance.fromItemStack(axe);
        int level = tool != null ? tool.getModifierLevel("chain_stripper") : 1;

        CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier("chain_stripper");
        if (modifier == null) return;
        CustomModifier.LevelSettings settings = modifier.getLevel(level);
        if (settings == null) return;

        Map<Material, Material> stripMap = loadStripMap(settings);
        Material startType = clickedBlock.getType();
        if (!stripMap.containsKey(startType)) return;

        int maxBlocks = settings.getBehaviorInt("max-blocks", 10);

        event.setCancelled(true);

        processingCustomBreak.set(true);
        try {
            triggerChainStripping(player, clickedBlock, startType, maxBlocks, axe, stripMap);
        } finally {
            processingCustomBreak.set(false);
        }
    }

    private Map<Material, Material> loadStripMap(CustomModifier.LevelSettings settings) {
        Map<Material, Material> stripMap = new HashMap<>();
        if (settings == null) return stripMap;
        Object logsObj = settings.getBehaviorSettings().get("logs");
        if (logsObj instanceof ConfigurationSection sec) {
            for (String fromKey : sec.getKeys(false)) {
                Material fromMat = Material.matchMaterial(fromKey);
                String toVal = sec.getString(fromKey);
                Material toMat = toVal != null ? Material.matchMaterial(toVal) : null;
                if (fromMat != null && toMat != null) {
                    stripMap.put(fromMat, toMat);
                }
            }
        } else if (logsObj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    Material fromMat = Material.matchMaterial(entry.getKey().toString());
                    Material toMat = Material.matchMaterial(entry.getValue().toString());
                    if (fromMat != null && toMat != null) {
                        stripMap.put(fromMat, toMat);
                    }
                }
            }
        }
        return stripMap;
    }

    private void triggerChainStripping(Player player, Block startBlock, Material targetMaterial, int maxBlocks, ItemStack tool, Map<Material, Material> stripMap) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        int strippedCount = 0;
        boolean playedSound = false;

        while (!queue.isEmpty() && strippedCount < maxBlocks) {
            Block current = queue.poll();
            Material currentType = current.getType();
            Material strippedType = stripMap.get(currentType);

            if (strippedType != null) {
                BlockBreakEvent virtualEvent = new BlockBreakEvent(current, player);
                Bukkit.getPluginManager().callEvent(virtualEvent);

                if (!virtualEvent.isCancelled()) {
                    BlockData blockData = current.getBlockData();
                    current.setType(strippedType);

                    if (blockData instanceof Orientable orientable) {
                        BlockData newBlockData = current.getBlockData();
                        if (newBlockData instanceof Orientable newOrientable) {
                            newOrientable.setAxis(orientable.getAxis());
                            current.setBlockData(newOrientable);
                        }
                    }

                    if (!playedSound) {
                        current.getWorld().playSound(current.getLocation(), Sound.ITEM_AXE_STRIP, 1.0f, 1.0f);
                        playedSound = true;
                    }

                    current.getWorld().spawnParticle(Particle.BLOCK, current.getLocation().add(0.5, 0.5, 0.5), 5, current.getBlockData());

                    applyDurabilityDamage(player, tool);
                    strippedCount++;

                    if (tool.getType() == Material.AIR || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                        return;
                    }
                }
            }

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        if (!visited.contains(neighbor) && stripMap.containsKey(neighbor.getType()) && isRelatedLog(targetMaterial, neighbor.getType())) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private boolean isRelatedLog(Material m1, Material m2) {
        if (m1 == m2) return true;
        String n1 = m1.name();
        String n2 = m2.name();
        String prefix1 = getLogPrefix(n1);
        String prefix2 = getLogPrefix(n2);
        return prefix1 != null && prefix1.equals(prefix2);
    }

    private String getLogPrefix(String name) {
        if (name.endsWith("_LOG")) return name.substring(0, name.length() - 4);
        if (name.endsWith("_WOOD")) return name.substring(0, name.length() - 5);
        if (name.endsWith("_STEM")) return name.substring(0, name.length() - 5);
        if (name.endsWith("_HYPHAE")) return name.substring(0, name.length() - 7);
        if (name.equals("BAMBOO_BLOCK")) return "BAMBOO";
        return null;
    }

    private void applyDurabilityDamage(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta() instanceof Damageable damageable) {
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (Math.random() < (1.0 / (unbreakingLevel + 1))) {
                int newDamage = damageable.getDamage() + 1;
                if (newDamage >= item.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(newDamage);
                    item.setItemMeta(damageable);
                }
            }
        }
    }
}
