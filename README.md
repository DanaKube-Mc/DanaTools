# 🌟 DanaTools — Progression d'Équipements & Modificateurs Custom (1.21.1)

**DanaTools** est un plugin Minecraft haut de gamme et entièrement configurable pour les serveurs **Paper/Spigot (1.21.1)**. Il réinvente la progression de l'équipement vanilla en introduisant des outils et des armures évolutifs (système d'XP et niveaux), des modificateurs modulaires à appliquer via la Table de Forgeron, ainsi qu'un débridage de l'Enclume bypassant la pénalité « Trop Cher ! ».

---

## 🚀 1. Fonctionnalités Clés

* **📈 Progression Active et Passive :**
  - **Outils :** Progression via des activités comme le minage, le bûcheronnage, le labourage, l'écorçage ou la pêche.
  - **Armures :** Progression via les dégâts de combat absorbés ou la distance d'exploration parcourue à pied.
* **🔨 Système de Forge Modulaire (Smithing Table) :**
  - Application et amélioration de modificateurs à l'aide de plaques de template personnalisées et de noyaux élémentaires.
  - Consommation dynamique des slots d'équipement selon le coût requis.
* **⚡ Enclume et Enchantements Débridés :**
  - Fixation de limites d'enchantement personnalisées par outil (ex: *Efficacité XV*).
  - Suppression de la pénalité de coût cumulé vanilla (« Trop Cher ! ») avec tarification fixe ou dynamique en niveaux d'XP (maximum 30 niveaux).
  - Fusion intelligente des livres custom de haut niveau et des équipements.
* **💎 Noyaux Élémentaires Aléatoires :**
  - Dropping chance de noyaux de forge lors du cassage de blocs d'XP (effets de particules, action bar et sons configurables).
* **🛡️ Gestion Sécurisée et Intégrations :**
  - Prise en charge d'ItemsAdder / Resource packs custom via les NBT de `CustomModelData`.
  - Intégration de skins Base64 pour les têtes de joueurs représentant les noyaux.
  - Génération d'événements virtuels pour respecter les plugins de protection de territoire (WorldGuard, GriefPrevention, etc.).

---

## 📂 2. Architecture Technique (Java)

Le projet utilise un modèle **Multi-Service / Multi-Manager** découplé pour une efficacité maximale et une maintenance facilitée.

* **[DanaTools.java](https://github.com/DanaKube-Mc/DanaTools/blob/main/src/main/java/com/danakube/danatools/DanaTools.java) :** Point d'entrée du plugin, gère le chargement et le déchargement de tous les services et gestionnaires.
* **[DanaItemInstance.java](https://github.com/DanaKube-Mc/DanaTools/blob/main/src/main/java/com/danakube/danatools/model/DanaItemInstance.java) :** Wrapper d'objet Minecraft stockant et manipulant les données de progression (`level`, `xp`, `modifiers`, `slots`) dans le `PersistentDataContainer` (PDC).
* **[DanaModifier.java](https://github.com/DanaKube-Mc/DanaTools/blob/main/src/main/java/com/danakube/danatools/modifier/DanaModifier.java) :** Classe abstraite auto-écoutante (*Self-Listening*) dont héritent tous les modificateurs pour réagir de manière autonome aux événements Minecraft.

```
                  +-----------------------------------+
                  |        DanaTools (Core)           |
                  +-----------------------------------+
                  /         |        |        \       \
                 v          v        v         v       v
            [Config]    [Storage] [Forge]  [Progression] [Modifier]
```

---

## 📖 3. Wiki Complet du Projet

La documentation est structurée de manière modulaire à la racine du projet dans le dossier `wiki/` :

1. 📈 **[Système de Progression](https://github.com/DanaKube-Mc/DanaTools/blob/main/wiki/01_systeme_progression.md) :** Courbe d'XP, formules de progression d'outils et d'armures, et fonctionnement de la *Résonance*.
2. 🔨 **[La Forge & la Smithing Table](https://github.com/DanaKube-Mc/DanaTools/blob/main/wiki/02_la_forge_smithing.md) :** Application, surcoûts en slots, validation de craft et whitelists par outil.
3. ⚡ **[Gestion de l'Enclume](https://github.com/DanaKube-Mc/DanaTools/blob/main/wiki/03_enclume_enchantements.md) :** Débridage des limites d'enchantements, fusions, bypass « Trop Cher ! » et fix de renommage.
4. 🔮 **[Catalogue des 20 Modificateurs](https://github.com/DanaKube-Mc/DanaTools/blob/main/wiki/04_catalogue_modificateurs.md) :** Fiches détaillées (Trench, VeinMiner, AutoSell, Magnet, Purify, etc.).
5. 🌍 **[Commandes & Configurations](https://github.com/DanaKube-Mc/DanaTools/blob/main/wiki/05_commandes_configurations.md) :** Paramétrage complet des fichiers YAML, traduction dans [lang/fr.yml](https://github.com/DanaKube-Mc/DanaTools/blob/main/src/main/resources/lang/fr.yml), et commandes d'administration.

---

## 🛠️ 4. Commandes d'Administration & Permissions

Toutes les commandes sont enregistrées sous la permission globale `danatools.admin`.

| Commande | Description |
| :--- | :--- |
| `/danatools give <tool_id> [player]` | Donne un outil/armure évolutif configuré. |
| `/danatools givemodifier <modifier_id> [player]` | Donne le template et le noyau requis pour forger ce modificateur. |
| `/danatools addxp <amount> [player]` | Ajoute de l'XP à l'outil/armure tenu en main principale. |
| `/danatools setlevel <level> [player]` | Définit le niveau de l'outil/armure tenu en main principale. |
| `/danatools reload` | Recharge l'intégralité des configurations et fichiers de langue. |

---

## ⚙️ 5. Compilation et Déploiement

Le plugin utilise **Maven** pour la gestion de ses dépendances et sa compilation.

### Prérequis
* **Java 21**
* **Maven 3.8+**

### Compilation
Pour compiler et packager le plugin, exécutez la commande suivante à la racine :
```bash
mvn clean package
```
Le fichier JAR produit se trouve dans le dossier `target/` sous le nom `danatools-1.0.0-SNAPSHOT.jar`. Copiez-le simplement dans le dossier `plugins/` de votre serveur Paper 1.21.1.
