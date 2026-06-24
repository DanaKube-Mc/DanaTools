package com.danakube.danatools.config;

import com.danakube.danatools.DanaTools;

import java.io.File;

public class ConfigManager {

    private final DanaTools plugin;
    private File toolsFolder;
    private File modifiersFolder;

    public ConfigManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void setupConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        plugin.saveDefaultConfig();

        saveResourceIfNotExists("lang/fr.yml");
        saveResourceIfNotExists("tools/heavy_pickaxe.yml");
        saveResourceIfNotExists("tools/heavy_hoe.yml");
        saveResourceIfNotExists("tools/heavy_axe.yml");
        saveResourceIfNotExists("modifiers/vein_miner.yml");
        saveResourceIfNotExists("modifiers/trench.yml");
        saveResourceIfNotExists("modifiers/wisdom.yml");
        saveResourceIfNotExists("modifiers/learning.yml");
        saveResourceIfNotExists("modifiers/auto_smelt.yml");
        saveResourceIfNotExists("modifiers/compactor.yml");
        saveResourceIfNotExists("modifiers/auto_sell.yml");
        saveResourceIfNotExists("modifiers/haste.yml");
        saveResourceIfNotExists("modifiers/night_vision.yml");
        saveResourceIfNotExists("modifiers/auto_replant.yml");
        saveResourceIfNotExists("modifiers/magnet.yml");
        saveResourceIfNotExists("modifiers/tiller.yml");
        saveResourceIfNotExists("modifiers/planter.yml");
        saveResourceIfNotExists("modifiers/chain_stripper.yml");
        saveResourceIfNotExists("modifiers/harvester.yml");
        saveResourceIfNotExists("modifiers/unbreakable.yml");

        this.toolsFolder = new File(plugin.getDataFolder(), "tools");
        this.modifiersFolder = new File(plugin.getDataFolder(), "modifiers");
    }

    public File getToolsFolder() {
        return toolsFolder;
    }

    public File getModifiersFolder() {
        return modifiersFolder;
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            try {
                plugin.saveResource(resourcePath, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Impossible de copier la ressource par defaut : " + resourcePath);
            }
        }
    }
}
