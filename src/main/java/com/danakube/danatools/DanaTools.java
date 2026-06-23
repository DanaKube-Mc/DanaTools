package com.danakube.danatools;

import com.danakube.danatools.command.DanaToolsCommand;
import com.danakube.danatools.config.ConfigManager;
import com.danakube.danatools.config.LangManager;
import com.danakube.danatools.config.ModifierConfigManager;
import com.danakube.danatools.config.ToolConfigManager;
import com.danakube.danatools.forge.AnvilListener;
import com.danakube.danatools.forge.ForgeRecipeRegistry;
import com.danakube.danatools.forge.SmithingListener;
import com.danakube.danatools.modifier.ModifierRegistry;
import com.danakube.danatools.modifier.CompactorManager;
import com.danakube.danatools.progression.BlockBreakXPListener;
import com.danakube.danatools.progression.XPManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class DanaTools extends JavaPlugin {

    private static DanaTools instance;

    private ConfigManager configManager;
    private LangManager langManager;
    private ToolConfigManager toolConfigManager;
    private ModifierConfigManager modifierConfigManager;
    private ForgeRecipeRegistry forgeRecipeRegistry;
    private ModifierRegistry modifierRegistry;
    private XPManager xpManager;
    private CompactorManager compactorManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Initialisation de DanaTools...");

        this.configManager = new ConfigManager(this);
        this.configManager.setupConfigs();

        this.langManager = new LangManager(this);
        this.langManager.loadLang();

        this.toolConfigManager = new ToolConfigManager(this);
        this.toolConfigManager.loadTools();

        this.modifierConfigManager = new ModifierConfigManager(this);
        this.modifierConfigManager.loadModifiers();

        this.forgeRecipeRegistry = new ForgeRecipeRegistry(this);
        this.forgeRecipeRegistry.registerRecipes();

        this.xpManager = new XPManager(this);

        this.compactorManager = new CompactorManager();
        this.compactorManager.loadRecipes();

        this.modifierRegistry = new ModifierRegistry(this);
        this.modifierRegistry.registerDefaultModifiers();

        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakXPListener(this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);

        DanaToolsCommand cmd = new DanaToolsCommand(this);
        getCommand("danatools").setExecutor(cmd);
        getCommand("danatools").setTabCompleter(cmd);

        getLogger().info("DanaTools initialise avec succes !");
    }

    @Override
    public void onDisable() {
        if (this.forgeRecipeRegistry != null) {
            this.forgeRecipeRegistry.unregisterRecipes();
        }
        getLogger().info("DanaTools desactive !");
    }

    public static DanaTools getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public ToolConfigManager getToolConfigManager() {
        return toolConfigManager;
    }

    public ModifierConfigManager getModifierConfigManager() {
        return modifierConfigManager;
    }

    public ModifierRegistry getModifierRegistry() {
        return modifierRegistry;
    }

    public XPManager getXpManager() {
        return xpManager;
    }

    public CompactorManager getCompactorManager() {
        return compactorManager;
    }

    public void reloadPlugin() {
        try {
            this.configManager.setupConfigs();
            reloadConfig();
            this.langManager.loadLang();
            this.toolConfigManager.loadTools();
            this.modifierConfigManager.loadModifiers();
            if (this.compactorManager != null) {
                this.compactorManager.loadRecipes();
            }
            if (this.forgeRecipeRegistry != null) {
                this.forgeRecipeRegistry.registerRecipes();
            }
            getLogger().info("Configuration de DanaTools rechargee avec succes !");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erreur lors du rechargement de la configuration", e);
        }
    }
}
