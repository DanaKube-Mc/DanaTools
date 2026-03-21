package com.babydev.danatools;

import com.babydev.danatools.commands.ToolsCommand;
import com.babydev.danatools.listeners.MenuListener;
import com.babydev.danatools.listeners.ToolProtectionListener;
import com.babydev.danatools.listeners.ToolXPListener;
import com.babydev.danatools.listeners.ToolEffectListener;
import com.babydev.danatools.managers.PlayerStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class DanaTools extends JavaPlugin {

    public static NamespacedKey TOOL_ID_KEY;

    private FileConfiguration permissionsConfig;
    private File permissionsConfigFile;

    private FileConfiguration langConfig;
    private File langConfigFile;

    private final Map<String, FileConfiguration> toolsConfigs = new HashMap<>();
    private PlayerStatsManager playerStatsManager;
    private ToolXPListener toolXPListener;
    private ToolEffectListener toolEffectListener;
    private Economy econ = null;

    @Override
    public void onEnable() {
        TOOL_ID_KEY = new NamespacedKey(this, "tool_id");

        // Sauvegarde de la configuration par défaut (config.yml)
        saveDefaultConfig();
        updateConfigIfMissingKeys(getConfig(), new File(getDataFolder(), "config.yml"), "config.yml");

        if (getConfig().getBoolean("settings.use_vault", false)) {
            if (!setupEconomy()) {
                getLogger().warning("Vault ou plugin d'économie introuvable. L'économie est désactivée !");
            }
        }

        // Création et chargement du fichier permissions.yml
        createPermissionsConfig();

        // Création et chargement du fichier de langue
        createLangConfig();

        // Initialisation du gestionnaire de stats des joueurs
        this.playerStatsManager = new PlayerStatsManager(this);

        // Chargement des outils depuis le dossier tools/
        loadToolsConfigs();

        // Enregistrement de la commande /tools
        getCommand("tools").setExecutor(new ToolsCommand(this));

        // Enregistrement de nos Listeners
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ToolProtectionListener(this), this);
        this.toolXPListener = new ToolXPListener(this);
        Bukkit.getPluginManager().registerEvents(this.toolXPListener, this);
        this.toolEffectListener = new ToolEffectListener(this);
        Bukkit.getPluginManager().registerEvents(this.toolEffectListener, this);

    }

    public ToolXPListener getToolXPListener() {
        return this.toolXPListener;
    }

    public ToolEffectListener getToolEffectListener() {
        return this.toolEffectListener;
    }

    public Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public FileConfiguration getPermissionsConfig() {
        return this.permissionsConfig;
    }

    public void reloadPermissionsConfig() {
        if (permissionsConfigFile == null) {
            permissionsConfigFile = new File(getDataFolder(), "permissions.yml");
        }
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionsConfigFile);
    }

    private void createPermissionsConfig() {
        permissionsConfigFile = new File(getDataFolder(), "permissions.yml");
        if (!permissionsConfigFile.exists()) {
            permissionsConfigFile.getParentFile().mkdirs();
            saveResource("permissions.yml", false);
        }
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionsConfigFile);
        updateConfigIfMissingKeys(permissionsConfig, permissionsConfigFile, "permissions.yml");
    }

    public FileConfiguration getLangConfig() {
        return this.langConfig;
    }

    public void reloadLangConfig() {
        String lang = getConfig().getString("language", "fr");
        if (langConfigFile == null || !langConfigFile.getName().equals(lang + ".yml")) {
            langConfigFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
    }

    private void createLangConfig() {
        String lang = getConfig().getString("language", "fr");
        langConfigFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        if (!langConfigFile.exists()) {
            langConfigFile.getParentFile().mkdirs();
            try {
                saveResource("lang/" + lang + ".yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    langConfigFile.createNewFile();
                } catch (Exception ex) {
                    getLogger().warning("Impossible de créer le fichier " + langConfigFile.getName());
                }
            }
        }
        langConfig = YamlConfiguration.loadConfiguration(langConfigFile);
        updateConfigIfMissingKeys(langConfig, langConfigFile, "lang/" + lang + ".yml");
    }

    public String getMessage(String path) {
        String prefix = langConfig.getString("prefix", "§a[DanaTools] §f");
        String message = langConfig.getString(path, "Message introuvable: " + path);

        if (message.startsWith("§c") || message.startsWith("§a/")) {
            return message;
        }
        return prefix + message;
    }

    public String getRawMessage(String path) {
        return langConfig.getString(path, "Message introuvable: " + path);
    }

    public Map<String, FileConfiguration> getToolsConfigs() {
        return toolsConfigs;
    }

    public PlayerStatsManager getPlayerStatsManager() {
        return playerStatsManager;
    }

    public void loadToolsConfigs() {
        toolsConfigs.clear();
        File toolsDir = new File(getDataFolder(), "tools");

        if (!toolsDir.exists()) {
            toolsDir.mkdirs();
            saveResource("tools/_default.yml", false);
        }

        File[] files = toolsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("_")) {
                    continue;
                }
                String toolId = file.getName().replace(".yml", "");
                toolsConfigs.put(toolId, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    private void updateConfigIfMissingKeys(FileConfiguration currentConfig, File configFile, String resourceName) {
        java.io.InputStream defaultStream = getResource(resourceName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(defaultStream, java.nio.charset.StandardCharsets.UTF_8));
            boolean changed = false;
            for (String key : defaultConfig.getKeys(true)) {
                if (!currentConfig.contains(key)) {
                    currentConfig.set(key, defaultConfig.get(key));
                    changed = true;
                }
            }
            if (changed) {
                try {
                    currentConfig.save(configFile);
                } catch (java.io.IOException e) {
                    getLogger().warning("Impossible de mettre à jour le fichier " + configFile.getName());
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (playerStatsManager != null) {
            playerStatsManager.saveAndClearCache();
        }
    }
}
