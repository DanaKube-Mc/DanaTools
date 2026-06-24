package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.ToolInstance;
import com.danakube.danatools.modifier.DanaModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public class PlanterModifier extends DanaModifier {

    public PlanterModifier() {
        super("planter");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!isEquipped(player)) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        ItemStack hoe = event.getItem();
        if (hoe == null) return;

        ToolInstance tool = ToolInstance.fromItemStack(hoe);
        int level = tool != null ? tool.getModifierLevel("planter") : 1;

        CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier("planter");
        if (modifier == null) return;
        CustomModifier.LevelSettings settings = modifier.getLevel(level);
        if (settings == null) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> cropsConfig = (Map<String, Object>) settings.getBehaviorSettings().get("crops");
        if (cropsConfig == null) return;

        Material seed = detectSeed(player, cropsConfig);
        if (seed == null) return;

        Material soilRequired = (seed == Material.NETHER_WART) ? Material.SOUL_SAND : Material.FARMLAND;
        if (clickedBlock.getType() != soilRequired && !(seed == Material.NETHER_WART && clickedBlock.getType() == Material.SOUL_SOIL)) {
            return;
        }

        int range = settings.getBehaviorInt("range", 1);
        event.setCancelled(true);

        String cropBlockName = cropsConfig.get(seed.name()).toString();
        Material cropBlockType = Material.getMaterial(cropBlockName);
        if (cropBlockType == null) return;

        processingCustomBreak.set(true);
        try {
            triggerAreaPlanting(player, clickedBlock, range, seed, cropBlockType, hoe);
        } finally {
            processingCustomBreak.set(false);
        }
    }

    private Material detectSeed(Player player, Map<String, Object> cropsConfig) {
        PlayerInventory inv = player.getInventory();
        
        ItemStack offHand = inv.getItemInOffHand();
        if (offHand != null && cropsConfig.containsKey(offHand.getType().name())) {
            return offHand.getType();
        }

        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && cropsConfig.containsKey(item.getType().name())) {
                return item.getType();
            }
        }
        return null;
    }

    private void triggerAreaPlanting(Player player, Block centerBlock, int range, Material seed, Material cropBlockType, ItemStack tool) {
        int startX = centerBlock.getX();
        int startY = centerBlock.getY();
        int startZ = centerBlock.getZ();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                Block targetSoil = centerBlock.getWorld().getBlockAt(startX + x, startY, startZ + z);
                
                if (targetSoil.getType() == Material.FARMLAND || (seed == Material.NETHER_WART && (targetSoil.getType() == Material.SOUL_SAND || targetSoil.getType() == Material.SOUL_SOIL))) {
                    Block targetAir = targetSoil.getRelative(BlockFace.UP);
                    
                    if (targetAir.getType() == Material.AIR) {
                        if (!hasSeed(player, seed)) {
                            return;
                        }

                        BlockBreakEvent virtualEvent = new BlockBreakEvent(targetAir, player);
                        Bukkit.getPluginManager().callEvent(virtualEvent);

                        if (!virtualEvent.isCancelled()) {
                            targetAir.setType(cropBlockType);

                            consumeSeed(player, seed);

                            applyDurabilityDamage(player, tool);

                            if (tool.getType() == Material.AIR || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        centerBlock.getWorld().playSound(centerBlock.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
    }

    private boolean hasSeed(Player player, Material seed) {
        return player.getInventory().containsAtLeast(new ItemStack(seed), 1);
    }

    private void consumeSeed(Player player, Material seed) {
        PlayerInventory inv = player.getInventory();
        ItemStack offHand = inv.getItemInOffHand();
        
        if (offHand != null && offHand.getType() == seed) {
            offHand.setAmount(offHand.getAmount() - 1);
            inv.setItemInOffHand(offHand.getAmount() <= 0 ? null : offHand);
            return;
        }

        inv.removeItem(new ItemStack(seed, 1));
    }

    private void applyDurabilityDamage(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta() instanceof Damageable damageable) {
            if (damageable.isUnbreakable()) return;
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
