package com.danakube.danatools.config;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.ToolInstance;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LangManager {

    private final DanaTools plugin;
    private YamlConfiguration langConfig;

    public LangManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void loadLang() {
        String lang = plugin.getConfig().getString("lang", "fr");
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Fichier de langue introuvable : " + langFile.getName() + ". Utilisation de fr.yml par defaut.");
            langFile = new File(plugin.getDataFolder(), "lang/fr.yml");
        }

        if (langFile.exists()) {
            this.langConfig = YamlConfiguration.loadConfiguration(langFile);
        } else {
            this.langConfig = new YamlConfiguration();
            plugin.getLogger().severe("Aucun fichier de langue n'a pu etre charge !");
        }
    }

    public Component getMessage(String key, Object... placeholders) {
        if (langConfig == null) {
            return Component.text("Lang config not loaded");
        }

        String raw = langConfig.getString(key);
        if (raw == null) {
            return Component.text("Missing translation key: " + key);
        }

        if (raw.contains("{prefix}")) {
            String prefix = langConfig.getString("prefix", "<green><b>[DanaTools]</b></green>");
            raw = raw.replace("{prefix}", prefix);
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length && placeholders[i] != null && placeholders[i + 1] != null) {
                String target = placeholders[i].toString();
                String replacement = placeholders[i + 1].toString();
                raw = raw.replace(target, replacement);
            }
        }

        return ToolInstance.parseColor(raw);
    }
}
