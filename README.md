# DanaTools 🛠️

Un plugin Minecraft d'apprentissage pour la version **1.21.1** (Paper API).

> [!IMPORTANT]
> Ce plugin nécessite **Java 21** au minimum pour fonctionner correctement sur votre serveur Minecraft.

## 📋 Prérequis

- **Java 21+**
- **Maven**
- Un serveur **Paper 1.21.1**

## 💻 Compilation

Pour compiler le plugin, utilisez la commande suivante :

```bash
mvn clean package
```

> [!TIP]
> Le fichier JAR sera généré dans le dossier `target/`. Pour une mise à jour rapide, vous pouvez utiliser un outil de reload automatique.

## Système d'Outils Évolutifs

Le plugin permet de créer des outils personnalisés via des fichiers YAML dans `src/main/resources/tools/`.

### Placeholders disponibles

Vous pouvez utiliser ces variables dans le `name` et le `lore` de vos outils :

| Placeholder      | Description                                  | Exemple |
| :--------------- | :------------------------------------------- | :------ |
| `%level%`        | Niveau actuel de l'outil                     | `5`     |
| `%xp%`           | XP actuel accumulé                           | `120`   |
| `%max_xp%`       | XP requis pour le prochain niveau            | `250`   |
| `%progress_bar%` | Barre de progression visuelle                |         |
| `%bonus%`        | Bonus d'XP actif actuel (en %)               | `+20%`  |
| `%toolid_level%` | Niveau spécifique (ex: `%sword_test_level%`) | `5`     |

### Configuration de l'XP et Progression

Vous pouvez choisir entre deux modes de progression pour l'XP requis :

```yaml
xp:
  use_equation_xp: true # true = utilise la formule mathématique ci-dessous
  required_base: 100 # XP pour le niveau 1
  required_multiplier: 50 # Ajout par niveau (niveau 1 = 100, niveau 2 = 150, etc.)


  # Si use_equation_xp est false, le plugin lit 'xp_required' dans chaque niveau.
```

Gains d'XP par action (Simple ou Avancé) :

```yaml
on_kill: 5 # Gain fixe
mobs:
  CREEPER:
    xp: 10
    requirements:
      level: 2 # Nécessite d'être niveau 2 pour gagner de l'xp sur ce mob
      permission: "plg.pro" # Nécessite une permission
on_block_break: 1
blocks:
  DIAMOND_ORE: 20
```

### Configuration Globale (`config.yml`)

- `messages_action_barre`: `true` par défaut. Affiche un message dans la barre d'action lors d'un gain d'XP.
- `settings.level_up_sound`: Son joué lors d'un passage de niveau.

## Structure des Niveaux (Évolution)

Le plugin utilise maintenant un système de paliers configurables. Chaque niveau peut modifier l'item physiquement.

```yaml
max_level: 10
levels:
  0: # Niveau de base
    material: IRON_SWORD # Changement de matériau
    name: "<gold>Épée de Recrue"
    permissions: # Permission pour débloquer/utiliser (Optionnel)
      - "danatools.tools.sword.level0"
    xp_required: 100 # Utilisé si use_equation_xp: false

  1: # Passage au niveau 1
    material: DIAMOND_SWORD
    glow: true # Effet de brillance
    unbreakable: true # Indestructible
    enchantments: # Liste des enchantements de ce niveau
      ench0:
        enchantment: unbreaking
        level: 2
    rewards: # Actions uniques lors du level up
      messages:
        - "§aFélicitations pour votre promotion !"
      commands:
        - "give %player% GOLD_INGOT 1"
      repair: true # Répare l'outil lors du up
```

> [!IMPORTANT]
> **Permissions** : Si une section `permissions` est présente, elle est vérifiée pour débloquer l'outil (niveau 0) et pour chaque passage de niveau supérieur.

> [!TIP]
> **Enchantements** : Utilisez les noms officiels Bukkit (ex: `DAMAGE_ALL`, `DIG_SPEED`, `LOOT_BONUS_BLOCKS`).

## Commandes et Permissions

### Commandes Joueurs

- `/tools` : Ouvre le menu pour récupérer ou ranger vos outils évolutifs.
- `/tools help` : Affiche l'aide des commandes.

### Commandes Administrateurs

Nécessitent la permission configurée dans `permissions.yml` (par défaut `danatools.admin`).

| Commande                                        | Description                                                           |
| :---------------------------------------------- | :-------------------------------------------------------------------- |
| `/tools reload`                                 | Recharge toutes les configurations (`config.yml`, `lang/`, `tools/`). |
| `/tools look <joueur>`                          | Ouvre le menu pour visualiser la progression d'un autre joueur.       |
| `/tools give <joueur> <id_outil> [niv]`         | Donne un outil spécifique au niveau indiqué (débloquage auto).        |
| `/tools addxp <joueur> <id_outil> <montant>`    | Ajoute de l'XP à l'outil d'un joueur (déclenche les level-ups).       |
| `/tools removexp <joueur> <id_outil> <montant>` | Retire de l'XP de l'outil d'un joueur (minimum 0).                    |
| `/tools set <joueur> <id_outil> level <valeur>` | Définit le niveau (remet l'XP à 0).                                   |
| `/tools set <joueur> <id_outil> xp <valeur>`    | Définit l'XP (bridé par le max du niveau actuel).                     |

### Configuration des Permissions (`permissions.yml`)

Vous pouvez personnaliser chaque permission :

```yaml
admin: "danatools.admin" # Accès aux commandes d'administration
reload: "danatools.reload" # Accès au reload uniquement
help: "danatools.help" # Accès au help uniquement
use_tools: "" # Laisser vide pour que tout le monde ait /tools
```

---

## 🚀 Prochaines fonctionnalités (Idées)

- **Effets de Potion** : Ajouter des effets passifs (Vitesse, Force) quand l'outil est tenu.
- **Attributs Natifs** : Bonus de vitesse d'attaque ou de PV via les attributs vanilla.
- **Arbre de compétences** : Plusieurs branches d'évolution possibles pour un même outil.
- **Cumul de bonus** : Permettre d'additionner plusieurs bonus d'XP au lieu de prendre le poids le plus fort.
