package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;

public class PurifyModifier extends DanaModifier {

    public PurifyModifier() {
        super("purify");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!isEquipped(player)) {
            return;
        }

        ItemStack toolItem = player.getInventory().getItemInMainHand();
        DanaItemInstance tool = DanaItemInstance.fromItemStack(toolItem);
        if (tool == null) {
            return;
        }

        int level = tool.getModifierLevel("purify");
        if (level <= 0) {
            return;
        }

        CustomModifier modifierConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("purify");
        if (modifierConfig == null) {
            return;
        }

        CustomModifier.LevelSettings settings = modifierConfig.getLevel(level);
        if (settings == null) {
            return;
        }

        Map<String, Object> targetsMap = loadTargetsConfig(settings);
        Entity target = event.getEntity();
        String sourceTypeName = target.getType().name();

        if (!targetsMap.containsKey(sourceTypeName)) {
            return;
        }

        if (target instanceof ZombieVillager zombieVillager && zombieVillager.isConverting()) {
            return;
        }

        double chance = settings.getBehaviorDouble("chance", 0.33);
        int durabilityCost = settings.getBehaviorInt("durability-cost", 5);

        if (Math.random() > chance) {
            return;
        }

        event.setCancelled(true);

        String destinationTypeName = targetsMap.get(sourceTypeName).toString().toUpperCase();

        if (sourceTypeName.equals("ZOMBIE_VILLAGER")) {
            ZombieVillager zombieVillager = (ZombieVillager) target;
            zombieVillager.setConversionTime(20);
            zombieVillager.setConversionPlayer(player);
        } else if (sourceTypeName.equals("PIG_ZOOMBIE") || sourceTypeName.equals("PIG_ZOMBIE")) {
            PigZombie zombifiedPiglin = (PigZombie) target;
            Class<? extends LivingEntity> targetClass = Piglin.class;
            
            if (destinationTypeName.equals("PIGLIN_BRUTE") || destinationTypeName.equals("PIGLIN")) {
                ItemStack mainHand = zombifiedPiglin.getEquipment().getItemInMainHand();
                if (mainHand != null && mainHand.getType() == Material.GOLDEN_AXE) {
                    targetClass = PiglinBrute.class;
                } else if (destinationTypeName.equals("PIGLIN_BRUTE")) {
                    targetClass = PiglinBrute.class;
                }
            } else {
                try {
                    org.bukkit.entity.EntityType destType = org.bukkit.entity.EntityType.valueOf(destinationTypeName);
                    Class<? extends Entity> entClass = destType.getEntityClass();
                    if (entClass != null && LivingEntity.class.isAssignableFrom(entClass)) {
                        targetClass = entClass.asSubclass(LivingEntity.class);
                    }
                } catch (Exception ignored) {}
            }

            LivingEntity curedEntity = zombifiedPiglin.getWorld().spawn(zombifiedPiglin.getLocation(), targetClass);

            curedEntity.getEquipment().setItemInMainHand(zombifiedPiglin.getEquipment().getItemInMainHand());
            curedEntity.getEquipment().setItemInOffHand(zombifiedPiglin.getEquipment().getItemInOffHand());
            curedEntity.getEquipment().setHelmet(zombifiedPiglin.getEquipment().getHelmet());
            curedEntity.getEquipment().setChestplate(zombifiedPiglin.getEquipment().getChestplate());
            curedEntity.getEquipment().setLeggings(zombifiedPiglin.getEquipment().getLeggings());
            curedEntity.getEquipment().setBoots(zombifiedPiglin.getEquipment().getBoots());

            Component customName = zombifiedPiglin.customName();
            if (customName != null) {
                curedEntity.customName(customName);
                curedEntity.setCustomNameVisible(zombifiedPiglin.isCustomNameVisible());
            }

            if (curedEntity instanceof Piglin piglin) {
                piglin.setImmuneToZombification(true);
            }

            zombifiedPiglin.remove();
        } else {
            try {
                EntityType destType = EntityType.valueOf(destinationTypeName);
                Class<? extends Entity> targetClass = destType.getEntityClass();
                if (targetClass != null) {
                    Entity curedEntity = target.getWorld().spawn(target.getLocation(), targetClass);

                    Component customName = target.customName();
                    if (customName != null) {
                        curedEntity.customName(customName);
                        curedEntity.setCustomNameVisible(target.isCustomNameVisible());
                    }

                    if (target instanceof LivingEntity sourceLiving && curedEntity instanceof LivingEntity destLiving) {
                        destLiving.getEquipment().setItemInMainHand(sourceLiving.getEquipment().getItemInMainHand());
                        destLiving.getEquipment().setItemInOffHand(sourceLiving.getEquipment().getItemInOffHand());
                        destLiving.getEquipment().setHelmet(sourceLiving.getEquipment().getHelmet());
                        destLiving.getEquipment().setChestplate(sourceLiving.getEquipment().getChestplate());
                        destLiving.getEquipment().setLeggings(sourceLiving.getEquipment().getLeggings());
                        destLiving.getEquipment().setBoots(sourceLiving.getEquipment().getBoots());
                    }

                    target.remove();
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage(DanaTools.getInstance().getLangManager().getMessage("purify.invalid_entity", "{type}", destinationTypeName));
                return;
            }
        }

        applyDurabilityDamage(player, toolItem, durabilityCost);

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        target.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                target.getLocation().add(0, 1.0, 0),
                20,
                0.5,
                0.5,
                0.5,
                0.1
        );
    }

    private Map<String, Object> loadTargetsConfig(CustomModifier.LevelSettings settings) {
        Map<String, Object> targetsMap = new HashMap<>();
        if (settings == null) {
            return targetsMap;
        }
        Object targetsObj = settings.getBehaviorSettings().get("targets");
        if (targetsObj instanceof ConfigurationSection sec) {
            for (String key : sec.getKeys(false)) {
                targetsMap.put(key.toUpperCase(), sec.get(key));
            }
        } else if (targetsObj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    targetsMap.put(entry.getKey().toString().toUpperCase(), entry.getValue());
                }
            }
        }
        return targetsMap;
    }

    private void applyDurabilityDamage(Player player, ItemStack item, int amount) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        if (item.getItemMeta() instanceof Damageable damageable) {
            if (damageable.isUnbreakable()) {
                return;
            }
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            int finalDamage = 0;
            for (int i = 0; i < amount; i++) {
                if (Math.random() < (1.0 / (unbreakingLevel + 1))) {
                    finalDamage++;
                }
            }
            if (finalDamage > 0) {
                int newDamage = damageable.getDamage() + finalDamage;
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
