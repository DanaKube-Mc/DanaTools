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
import com.danakube.danatools.modifier.AutoSellManager;
import com.danakube.danatools.modifier.PotionModifierManager;
import com.danakube.danatools.modifier.PotionModifierListener;
import com.danakube.danatools.modifier.MagnetTask;
import com.danakube.danatools.progression.ToolXPListener;
import com.danakube.danatools.progression.XPManager;
import com.danakube.danatools.progression.ArmorExplorationTask;
import com.danakube.danatools.progression.ArmorXPListener;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    private AutoSellManager autoSellManager;
    private PotionModifierManager potionModifierManager;
    private ArmorExplorationTask armorExplorationTask;
    private Economy economy;

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

        setupEconomy();
        this.autoSellManager = new AutoSellManager(this);
        this.autoSellManager.loadPrices();

        this.potionModifierManager = new PotionModifierManager(this);

        this.modifierRegistry = new ModifierRegistry(this);
        this.modifierRegistry.registerDefaultModifiers();

        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolXPListener(this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new PotionModifierListener(this), this);

        this.armorExplorationTask = new ArmorExplorationTask();
        getServer().getPluginManager().registerEvents(new ArmorXPListener(this.armorExplorationTask), this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                this.potionModifierManager.checkAndApply(player);
            }
        }, 40L, 40L);

        getServer().getScheduler().runTaskTimer(this, new MagnetTask(), 10L, 10L);
        getServer().getScheduler().runTaskTimer(this, this.armorExplorationTask, 100L, 100L);

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

    public PotionModifierManager getPotionModifierManager() {
        return potionModifierManager;
    }

    public XPManager getXpManager() {
        return xpManager;
    }

    public CompactorManager getCompactorManager() {
        return compactorManager;
    }

    public AutoSellManager getAutoSellManager() {
        return autoSellManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        return this.economy != null;
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
            if (this.autoSellManager != null) {
                this.autoSellManager.loadPrices();
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
