# 🌍 Commandes & Fichiers de Configuration — DanaTools

Ce guide détaille l'utilisation des commandes administratives et présente la structure de tous les fichiers de configuration YAML utilisés par le plugin **DanaTools**.

---

## 1. Commandes & Permissions Administratives

Toutes les commandes administratives requièrent la permission **`danatools.admin`** (accordée par défaut aux opérateurs de serveurs).

* **`/danatools give <tool_id> [player]`**
  - *Aliases :* `/dtools give`, `/dt give`
  - *Description :* Distribue l'outil ou l'armure évolutive spécifié par son `<tool_id>` au joueur ciblé (ou à soi-même si omis).
  
* **`/danatools givemodifier <modifier_id> [player]`**
  - *Description :* Donne au joueur les deux composants de forge nécessaires pour forger ou améliorer le modificateur spécifié : la Plaque de Template (Slot 1) et le Noyau Élémentaire/Ingrédient (Slot 3).
  
* **`/danatools addxp <amount> [player]`**
  - *Description :* Ajoute un montant d'XP à l'outil ou l'armure actuellement tenu dans la main principale du joueur ciblé. Déclenche un level-up si le seuil requis est dépassé.
  
* **`/danatools setlevel <level> [player]`**
  - *Description :* Définit le niveau actuel de l'outil ou l'armure évolutive tenu dans la main principale. Réajuste automatiquement les slots libres et l'XP.
  
* **`/danatools reload`**
  - *Description :* Recharge l'intégralité des configurations (`config.yml`, outils, modificateurs) ainsi que les fichiers de langues sans redémarrer le serveur.

---

## 2. Configuration Globale (`config.yml`)

Ce fichier définit les paramètres généraux du plugin, notamment la langue et les alertes visuelles/sonores lors des drops de noyaux élémentaires.

```yaml
# Langue par défaut du plugin (doit correspondre à un fichier dans lang/ Ex: fr.yml)
lang: "fr"

# Configuration globale des effets lors du drop aléatoire d'un noyau
core-drop-settings:
  # Notification ActionBar au joueur
  action-bar:
    enabled: true
    message: "<green>[DanaTools] Vous avez trouvé un Noyau Élémentaire !" # Supporte Kyori MiniMessage
  # Son joué au joueur
  sound:
    enabled: true
    type: "BLOCK_AMETHYST_BLOCK_CHIME" # Nom du son Spigot/Bukkit
    volume: 1.0
    pitch: 1.2
  # Particules jaillissant du bloc brisé
  particles:
    enabled: true
    type: "HAPPY_VILLAGER" # Nom de la particule Spigot/Bukkit
    count: 15
    offset: 0.3
    speed: 0.1
```

---

## 3. Configuration des Outils (`tools/`)

Chaque équipement évolutif (outil ou armure) possède son propre fichier YAML sous le répertoire `/tools/`.

```yaml
id: "heavy_pickaxe"            # Identifiant unique de l'outil
material: "DIAMOND_PICKAXE"    # Type d'item Minecraft vanilla
custom-model-data: 10025       # CustomModelData utilisé pour les packs de textures (ItemsAdder)
display-name: "<gradient:#ff5555:#ffaacc>Pioche Lourde</gradient>" # Nom avec support gradient MiniMessage

# Structure de la progression de l'outil
progression:
  max-level: 20                # Niveau maximum atteignable
  slots:
    5: 1                       # Nombre de slots totaux débloqués à chaque palier de niveau
    10: 2
    15: 3
    20: 4
  max-slots: 4                 # Nombre de slots maximum à l'échelle finale

# Courbe d'XP requise par niveau
xp-curve:
  base: 100                    # XP requise au niveau 1
  multiplier: 1.5              # Coefficient de croissance (Formule : base * level^multiplier)

# Activités de récolte (progression passive d'XP et drop de noyaux)
block-activities:
  COAL_ORE:                    # Type de bloc brisé
    xp: 2                      # XP de progression gagnée par l'outil
    core-drop:
      modifier-id: "trench"    # ID du modificateur associé au noyau qui peut tomber
      chance-percent: 0.05     # Pourcentage de chance de drop (ex: 0.05%)
  DIAMOND_ORE:
    xp: 10
    core-drop:
      modifier-id: "trench"
      chance-percent: 1.0      # 1% de chance de drop

# Whitelist des modificateurs autorisés et niveau maximum applicable
allowed-modifiers:
  trench: 3                    # Excavation max niveau III
  vein_miner: 2                # Minage en veine max niveau II
  wisdom: 3                    # Sagesse max niveau III

# Limites d'enchantements custom bridées à l'enclume
enchantment-limits:
  EFFICIENCY: 15               # Efficacité XV maximum
  UNBREAKING: 10               # Solidité X maximum
  FORTUNE: 5                   # Fortune V maximum
```

---

## 4. Configuration des Modificateurs (`modifiers/`)

Chaque modificateur possède son propre fichier YAML sous le répertoire `/modifiers/`.

```yaml
id: "trench"                   # ID unique du modificateur
incompatible-modifiers:        # Liste des modificateurs mutuellement exclusifs
  - "vein_miner"

# Définition de la plaque de Template de Forge (Slot 1 de la Table de Forgeron)
template-item:
  material: "NETHERITE_UPGRADE_SMITHING_TEMPLATE"
  custom-model-data: 5001      # CMD de la plaque
  display-name: "&6Placard de Forge : Excavation" # Nom requis de la plaque

# Définition de l'ingrédient / Noyau (Slot 3 de la Table de Forgeron)
ingredient-item:
  material: "PLAYER_HEAD"      # Tête de joueur customisée
  texture: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjViNTVhM2I0OTlhNDk2YTkyODkyYjRlYTVmZGQ2NThkOWQ1N2UxNjgxMWI1MzVkM2M2M2NhNDhjYjU0YTkifX19" # Texture Base64
  display-name: "&eNoyau de Terre" # Nom requis de l'ingrédient

# Paramètres spécifiques à chaque niveau du modificateur
levels:
  1:
    display-name: "&6Modificateur : Excavation I" # Libellé affiché dans le lore de l'outil
    lore:
      - "&7Mine en zone 3x3."
      - "&dSlots requis : &f1"
    slot-cost: 1               # Coût cumulatif en slots requis
    min-tool-level: 5          # Niveau de l'outil requis pour appliquer ce niveau
    behavior:
      type: "TRENCH"           # Type de comportement technique lié au code Java
      range: 1                 # Paramètre de portée (1 = 3x3)
  2:
    display-name: "&6Modificateur : Excavation II"
    lore:
      - "&7Mine en zone 5x5."
      - "&dSlots requis : &f2"
    slot-cost: 2
    min-tool-level: 10
    behavior:
      type: "TRENCH"
      range: 2                 # (2 = 5x5)
```

---

## 5. Gestion de la Langue (`lang/`)

Le plugin supporte l'externalisation complète de ses messages sous le répertoire `/lang/` (ex: [lang/fr.yml](https://github.com/DanaKube-Mc/DanaTools/blob/main/src/main/resources/lang/fr.yml)).

### Clés et Messages Clés :
- `prefix` : Le préfixe global devant tous les messages du plugin.
- `no_permission` : Message affiché en cas de permission insuffisante.
- `xp.level_up` : Message de level-up envoyé au joueur (avec variable `{level}`).
- `modifiers.none` : Ligne affichée par défaut s'il n'y a aucun modificateur sur l'outil.
- `modifiers.format_with_level` : Format d'affichage d'un modificateur ayant plusieurs niveaux (ex: ` - Excavation (Lvl II)`).
- `modifiers.format_no_level` : Format d'affichage d'un modificateur à niveau unique (ex: ` - Auto-Cuisson`).
