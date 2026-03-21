package com.babydev.danatools.managers;

import com.babydev.danatools.DanaTools;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère le stockage des statistiques des joueurs (XP/Niveaux des outils).
 * Crée un fichier .yml par joueur dans le dossier "players/".
 */
public class PlayerStatsManager {

    private final DanaTools plugin;
    private final File playersFolder;
    private final Map<UUID, FileConfiguration> playerConfigs = new HashMap<>();

    public PlayerStatsManager(DanaTools plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "players");

        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    /**
     * Récupère la configuration d'un joueur, la charge si nécessaire.
     */
    public FileConfiguration getPlayerConfig(UUID uuid) {
        if (playerConfigs.containsKey(uuid)) {
            return playerConfigs.get(uuid);
        }

        File playerFile = new File(playersFolder, uuid + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Impossible de créer le fichier stats pour " + uuid);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        playerConfigs.put(uuid, config);
        return config;
    }

    /**
     * Sauvegarde les données d'un joueur sur le disque.
     */
    public void savePlayer(UUID uuid) {
        FileConfiguration config = playerConfigs.get(uuid);
        if (config == null)
            return;

        try {
            config.save(new File(playersFolder, uuid + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Erreur lors de la sauvegarde du fichier stats de " + uuid);
        }
    }

    /**
     * Récupère le niveau d'un outil spécifique pour un joueur.
     */
    public int getToolLevel(UUID uuid, String toolId) {
        return getPlayerConfig(uuid).getInt("tools." + toolId + ".level", 0);
    }

    /**
     * Récupère l'XP d'un outil spécifique pour un joueur.
     */
    public int getToolXP(UUID uuid, String toolId) {
        return getPlayerConfig(uuid).getInt("tools." + toolId + ".xp", 0);
    }

    /**
     * Définit le niveau et l'XP d'un outil et sauvegarde.
     */
    public void updateToolStats(UUID uuid, String toolId, int level, int xp) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("tools." + toolId + ".level", level);
        config.set("tools." + toolId + ".xp", xp);
        savePlayer(uuid);
    }

    /**
     * Récupère les skill points d'un outil pour un joueur.
     */
    public int getSkillPoints(UUID uuid, String toolId) {
        return getPlayerConfig(uuid).getInt("tools." + toolId + ".skill_points", 0);
    }

    /**
     * Définit les skill points d'un outil.
     */
    public void setSkillPoints(UUID uuid, String toolId, int points) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("tools." + toolId + ".skill_points", points);
        savePlayer(uuid);
    }

    /**
     * Récupère le niveau d'un skill spécifique pour un outil donné.
     */
    public int getSkillLevel(UUID uuid, String toolId, String skillId) {
        return getPlayerConfig(uuid).getInt("tools." + toolId + ".skills." + skillId, 0);
    }

    /**
     * Définit le niveau d'un skill.
     */
    public void setSkillLevel(UUID uuid, String toolId, String skillId, int level) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("tools." + toolId + ".skills." + skillId, level);
        savePlayer(uuid);
    }

    /**
     * Vérifie si un outil a été débloqué (si la section existe dans le fichier du
     * joueur).
     */
    public boolean isToolUnlocked(UUID uuid, String toolId) {
        return getPlayerConfig(uuid).getConfigurationSection("tools." + toolId) != null;
    }

    /**
     * Sauvegarde tous les joueurs chargés et vide le cache.
     * Utile lors d'un reload pour prendre en compte des modifs manuelles de fichiers.
     */
    public void saveAndClearCache() {
        for (UUID uuid : new java.util.HashSet<>(playerConfigs.keySet())) {
            savePlayer(uuid);
        }
        playerConfigs.clear();
    }
}
