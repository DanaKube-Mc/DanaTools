package com.danakube.danatools.modifier.impl;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import com.danakube.danatools.modifier.DanaModifier;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BeheadingModifier extends DanaModifier {

    private final Map<String, MobSetting> mobSettings = new HashMap<>();
    private boolean allowSpawnerMobs = true;

    public BeheadingModifier() {
        super("beheading");
        loadConfig();
    }

    public void loadConfig() {
        mobSettings.clear();
        File file = new File(DanaTools.getInstance().getDataFolder(), "modifiers/beheading.yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        allowSpawnerMobs = config.getBoolean("allow-spawner-mobs", true);
        
        ConfigurationSection mobsSec = config.getConfigurationSection("mobs");
        if (mobsSec != null) {
            for (String key : mobsSec.getKeys(false)) {
                ConfigurationSection sec = mobsSec.getConfigurationSection(key);
                if (sec != null) {
                    String matStr = sec.getString("material", "PLAYER_HEAD");
                    double chance = sec.getDouble("chance-percent", 0.0);
                    String texture = sec.getString("texture");
                    String displayName = sec.getString("display-name");
                    mobSettings.put(key.toUpperCase(), new MobSetting(matStr, chance, texture, displayName));
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.getEntity().getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(DanaTools.getInstance(), "from_spawner"),
                PersistentDataType.BYTE,
                (byte) 1
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!allowSpawnerMobs) {
            NamespacedKey spawnerKey = new NamespacedKey(DanaTools.getInstance(), "from_spawner");
            if (entity.getPersistentDataContainer().has(spawnerKey, PersistentDataType.BYTE)) {
                return;
            }
        }
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }

        ItemStack handItem = killer.getInventory().getItemInMainHand();
        if (handItem == null) {
            return;
        }

        String typeName = handItem.getType().name();
        if (!typeName.contains("SWORD") && !typeName.contains("AXE")) {
            return;
        }

        DanaItemInstance tool = DanaItemInstance.fromItemStack(handItem);
        if (tool == null || !tool.hasModifier("beheading")) {
            return;
        }

        int level = tool.getModifierLevel("beheading");
        if (level <= 0) {
            return;
        }

        String entityKey = event.getEntityType().name();
        MobSetting setting = mobSettings.get(entityKey);
        if (setting == null) {
            return;
        }

        CustomModifier config = DanaTools.getInstance().getModifierConfigManager().getModifier("beheading");
        double chanceMultiplier = 1.0;
        if (config != null) {
            CustomModifier.LevelSettings settings = config.getLevel(level);
            if (settings != null) {
                chanceMultiplier = settings.getBehaviorDouble("chance-multiplier", 1.0);
            }
        }

        double finalChance = (setting.chancePercent / 100.0) * chanceMultiplier;
        if (Math.random() > finalChance) {
            return;
        }

        Material headMat = Material.matchMaterial(setting.material);
        if (headMat == null) {
            headMat = Material.PLAYER_HEAD;
        }

        ItemStack headItem = new ItemStack(headMat);

        if (headMat == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
            if (skullMeta != null) {
                if (entity instanceof Player deadPlayer) {
                    skullMeta.setOwningPlayer(deadPlayer);
                } else if (setting.texture != null && !setting.texture.isEmpty()) {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.getProperties().add(new ProfileProperty("textures", setting.texture));
                    skullMeta.setPlayerProfile(profile);
                }
                headItem.setItemMeta(skullMeta);
            }
        }

        if (setting.displayName != null && !setting.displayName.isEmpty()) {
            headItem.editMeta(meta -> {
                meta.displayName(DanaItemInstance.parseColor(setting.displayName));
            });
        }

        event.getDrops().add(headItem);
    }

    private static class MobSetting {
        final String material;
        final double chancePercent;
        final String texture;
        final String displayName;

        MobSetting(String material, double chancePercent, String texture, String displayName) {
            this.material = material;
            this.chancePercent = chancePercent;
            this.texture = texture;
            this.displayName = displayName;
        }
    }
}
