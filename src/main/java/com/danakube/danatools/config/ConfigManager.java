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
        saveResourceIfNotExists("modifiers/vein_miner.yml");
        saveResourceIfNotExists("modifiers/trench.yml");
        saveResourceIfNotExists("modifiers/wisdom.yml");
        saveResourceIfNotExists("modifiers/learning.yml");
        saveResourceIfNotExists("modifiers/auto_smelt.yml");

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
