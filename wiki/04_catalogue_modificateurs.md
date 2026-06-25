# 🔮 Catalogue Global des Modificateurs — DanaTools

Ce catalogue répertorie les **20 modificateurs** disponibles pour les outils et armures évolutifs du plugin **DanaTools**. Chaque modificateur s'applique et s'améliore via la **Table de Forgeron** (Smithing Table).

---

## 1. Table Récapitulative des Modificateurs

| ID | Nom en Jeu | Ingrédient principal | Incompatible avec | Description Rapide |
| :--- | :--- | :--- | :--- | :--- |
| [`trench`](#1-excavation-trench) | Excavation | Noyau de Terre (Tête Custom) | `vein_miner` | Mine en zone (3x3, 5x5, 7x7) |
| [`vein_miner`](#2-minage-en-veine-vein_miner) | Minage en Veine | Noyau de Vie (Clay Ball) | `trench` | Détruit les blocs de minerais/bois adjacents en chaîne |
| [`wisdom`](#3-sagesse-wisdom) | Sagesse | Essence d'Expérience (Fiole) | Aucun | Boost le gain d'XP vanilla du joueur (+15%, +30%, +50%) |
| [`learning`](#4-apprentissage-learning) | Apprentissage | Tome de Connaissance (Livre) | Aucun | Boost le gain d'XP de progression de l'outil (+20%, +40%, +60%) |
| [`auto_smelt`](#5-auto-cuisson-auto_smelt) | Auto-Cuisson | Noyau de Feu (Bâton de Blaze) | Aucun | Cuit directement le butin récolté (lingots, viandes, poissons) |
| [`compactor`](#6-compaction-compactor) | Compaction | Noyau de Pression (Piston) | Aucun | Compacte automatiquement les ressources de l'inventaire en blocs |
| [`auto_sell`](#7-auto-vente-auto_sell) | Auto-Vente | Noyau de Richesse (Bloc Émeraude) | Aucun | Vend automatiquement le butin récolté contre de la monnaie Vault |
| [`haste`](#8-célérité-haste) | Célérité | Noyau de Célérité (Sucre) | Aucun | Confère l'effet de potion Célérité I en main ou porté |
| [`night_vision`](#9-vision-nocturne-night_vision) | Vision Nocturne | Noyau de Vision (Carotte Dorée) | Aucun | Confère l'effet de potion Vision Nocturne en main ou porté |
| [`auto_replant`](#10-replantation-auto_replant) | Replantation | Noyau Fertile (Graines) | Aucun | Replante automatiquement les cultures mûres récoltées |
| [`magnet`](#11-magnétisme-magnet) | Magnétisme | Noyau Magnétique (Magnétite) | Aucun | Aspire automatiquement les items au sol dans un rayon autour |
| [`tiller`](#12-labourage-tiller) | Labourage | Noyau Agricole (Paille) | Aucun | Laboure la terre dans une zone (3x3, 5x5, 7x7) lors du clic droit |
| [`planter`](#13-semoir-planter) | Semoir | Noyau de Dispersion (Distributeur) | Aucun | Plante les graines de la main gauche en zone (3x3, 5x5, 7x7) |
| [`chain_stripper`](#14-écorceur-en-chaîne-chain_stripper) | Écorceur en Chaîne | Noyau de Coupe (Cisaille) | Aucun | Écorce (strip) de multiples bûches connectées en un clic droit |
| [`harvester`](#15-moissonneur-harvester) | Moissonneur | Noyau de Croissance (Poudre d'Os)| Aucun | Moissonne les cultures dans une zone (3x3, 5x5, 7x7) lors de la casse |
| [`unbreakable`](#16-indestructible-unbreakable) | Indestructible | Étoile d'Indestructibilité (Nether) | Aucun | Rend l'outil totalement insensible à la perte de durabilité |
| [`bouncy`](#17-rebond-bouncy) | Rebond | Noyau Élastique (Bloc de Slime) | Aucun | Annule les dégâts de chute et fait rebondir le joueur |
| [`purify`](#18-purificateur-purify) | Purificateur | Noyau de Purification (Pomme d'Or)| Aucun | Guérit ou convertit les créatures ciblées en les frappant |
| [`feather_step`](#19-pas-de-plume-feather_step) | Pas de Plume | Plume Céleste (Plume) | Aucun | Empêche de piétiner, d'activer les plaques ou de faire du bruit |
| [`resonance`](#20-résonance-resonance) | Résonance | Noyau Résonant (Quartz du Nether) | Aucun | Partage l'XP gagnée par un outil avec les pièces d'armure |

---

## 2. Détails des Modificateurs

### 1. Excavation (`trench`)
*Mine dans une zone carrée perpendiculaire à la face du bloc ciblée par le joueur.*
* **ID :** `trench` (Incompatible avec `vein_miner`)
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5001`) — *&6Placard de Forge : Excavation*
* **Ingrédient (Slot 3) :** `PLAYER_HEAD` (Noyau de Terre Base64) — *&eNoyau de Terre*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Zone **3x3** (Rayon 1)
  - **II :** Coût **2 slots**, Outil requis **Niv. 10**, Zone **5x5** (Rayon 2)
  - **III :** Coût **4 slots**, Outil requis **Niv. 15**, Zone **7x7** (Rayon 3)
* **Logique :** Écoute `BlockBreakEvent`. Vérifie les protections (WorldGuard) par événements virtuels et consomme de la durabilité proportionnellement.

### 2. Minage en Veine (`vein_miner`)
*Détruit récursivement en chaîne les blocs de minerais ou de bois adjacents.*
* **ID :** `vein_miner` (Incompatible avec `trench`)
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5001`) — *&6Placard de Forge : Minage en Veine*
* **Ingrédient (Slot 3) :** `CLAY_BALL` (CMD: `12001`) — *&eNoyau de Vie*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Max **32 blocs**
  - **II :** Coût **2 slots**, Outil requis **Niv. 12**, Max **64 blocs**
* **Logique :** Recherche récursive BFS (26 directions 3D). Déclenche des événements virtuels pour respecter les protections et s'arrête si l'outil tombe à zéro durabilité.

### 3. Sagesse (`wisdom`)
*Augmente l'expérience Vanilla gagnée par le joueur.*
* **ID :** `wisdom`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5002`) — *&6Placard de Forge : Sagesse*
* **Ingrédient (Slot 3) :** `EXPERIENCE_BOTTLE` — *&bEssence d'Expérience*
* **Niveaux :**
  - **I :** Coût **1 slot**, Équipement requis **Niv. 5**, **+15%** XP Joueur
  - **II :** Coût **2 slots**, Équipement requis **Niv. 10**, **+30%** XP Joueur
  - **III :** Coût **4 slots**, Équipement requis **Niv. 15**, **+50%** XP Joueur
* **Logique :** Intercepte les gains d'XP lors du minage, du combat et de la pêche. Multiplie l'XP finale reçue.

### 4. Apprentissage (`learning`)
*Augmente l'XP accumulée par l'équipement pour son propre niveau.*
* **ID :** `learning`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5003`) — *&6Placard de Forge : Apprentissage*
* **Ingrédient (Slot 3) :** `BOOK` — *&eTome de Connaissance*
* **Niveaux :**
  - **I :** Coût **1 slot**, Équipement requis **Niv. 5**, **+20%** XP Outil/Armure
  - **II :** Coût **2 slots**, Équipement requis **Niv. 12**, **+40%** XP Outil/Armure
  - **III :** Coût **4 slots**, Équipement requis **Niv. 18**, **+60%** XP Outil/Armure
* **Logique :** Intercepté lors de l'application d'XP dans `addXP(...)` pour appliquer le multiplicateur.

### 5. Auto-Cuisson (`auto_smelt`)
*Cuit instantanément le butin récolté.*
* **ID :** `auto_smelt` (Incompatible avec l'enchantement *Toucher de Soie*)
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5004`) — *&6Placard de Forge : Auto-Cuisson*
* **Ingrédient (Slot 3) :** `BLAZE_ROD` — *&eNoyau de Feu*
* **Niveaux :**
  - **I :** Coût **2 slots**, Outil requis **Niv. 8**, Cuit **100%** du butin récolté
* **Logique :** Intercepte les cassages et récoltes. Remplace les loots par leurs équivalents cuits (fer en lingot de fer, poisson cru en cuit, etc.). Compatible avec Fortune.

### 6. Compaction (`compactor`)
*Compacte automatiquement les ressources de l'inventaire en blocs.*
* **ID :** `compactor` (Incompatible avec l'enchantement *Toucher de Soie*)
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5005`) — *&6Placard de Forge : Compaction*
* **Ingrédient (Slot 3) :** `PISTON` — *&eNoyau de Pression*
* **Niveaux :**
  - **I :** Coût **2 slots**, Outil requis **Niv. 10**, Compactage automatique (9 $\to$ 1 bloc, 4 $\to$ 1 bloc)
* **Logique :** Intercepte `EntityPickupItemEvent`. Attend 1 tick pour que l'item soit présent dans l'inventaire, calcule le stock, retire les ingrédients et dépose le bloc produit.

### 7. Auto-Vente (`auto_sell`)
*Vend instantanément le butin récolté contre de l'argent de serveur.*
* **ID :** `auto_sell` (Incompatible avec l'enchantement *Toucher de Soie*)
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5006`) — *&6Placard de Forge : Auto-Vente*
* **Ingrédient (Slot 3) :** `EMERALD_BLOCK` — *&aNoyau de Richesse*
* **Niveaux :**
  - **I :** Coût **2 slots**, Outil requis **Niv. 10**, Vend à **80%** du prix configuré
  - **II :** Coût **3 slots**, Outil requis **Niv. 15**, Vend à **100%** du prix configuré
  - **III :** Coût **4 slots**, Outil requis **Niv. 20**, Vend à **120%** du prix configuré (bonus)
* **Logique :** Intercepte les drops de ressources configurées et transfère les gains sur le compte Vault du joueur. Affiche un ActionBar coloré.

### 8. Célérité (`haste`)
*Confère l'effet Célérité I au porteur.*
* **ID :** `haste`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5007`) — *&6Placard de Forge : Célérité*
* **Ingrédient (Slot 3) :** `SUGAR` — *&eNoyau de Célérité*
* **Niveaux :**
  - **I :** Coût **1 slot**, Équipement requis **Niv. 5**, Célérité I
* **Logique :** Géré par un scheduler global qui applique et retire l'effet de potion au joueur selon l'état de ses équipements tenus ou portés.

### 9. Vision Nocturne (`night_vision`)
*Confère l'effet Vision Nocturne au porteur.*
* **ID :** `night_vision`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5008`) — *&6Placard de Forge : Vision Nocturne*
* **Ingrédient (Slot 3) :** `GOLDEN_CARROT` — *&9Noyau de Vision*
* **Niveaux :**
  - **I :** Coût **1 slot**, Équipement requis **Niv. 5**, Vision Nocturne (durée 320 ticks pour éviter le clignotement)
* **Logique :** Géré par un scheduler global appliquant l'effet de potion en main ou porté.

### 10. Replantation (`auto_replant`)
*Replante automatiquement les cultures mûres.*
* **ID :** `auto_replant`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5009`) — *&6Placard de Forge : Replantation*
* **Ingrédient (Slot 3) :** `WHEAT_SEEDS` — *&aNoyau Fertile*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Replante en consommant une graine du butin
* **Logique :** Écoute le cassage d'une culture mûre. Re-dépose instantanément une graine au sol au stade initial (Age 0) en la soustrayant du butin récolté.

### 11. Magnétisme (`magnet`)
*Aspire automatiquement les items au sol dans un rayon autour.*
* **ID :** `magnet`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5010`) — *&6Placard de Forge : Magnétisme*
* **Ingrédient (Slot 3) :** `LODESTONE` — *&7Noyau Magnétique*
* **Niveaux :**
  - **I :** Coût **1 slot**, Équipement requis **Niv. 5**, Rayon de **4 blocs**
  - **II :** Coût **2 slots**, Équipement requis **Niv. 10**, Rayon de **8 blocs**
  - **III :** Coût **3 slots**, Équipement requis **Niv. 15**, Rayon de **12 blocs**
* **Logique :** Tâche répétitive (toutes les 10 ticks). Aspire les items vers l'inventaire si de la place est disponible.

### 12. Labourage (`tiller`)
*Laboure les blocs de terre et d'herbe en zone.*
* **ID :** `tiller`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5011`) — *&6Placard de Forge : Labourage*
* **Ingrédient (Slot 3) :** `HAY_BLOCK` — *&aNoyau Agricole*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Zone **3x3** (Rayon 1)
  - **II :** Coût **2 slots**, Outil requis **Niv. 10**, Zone **5x5** (Rayon 2)
  - **III :** Coût **4 slots**, Outil requis **Niv. 15**, Zone **7x7** (Rayon 3)
* **Logique :** Clic droit au sol (`PlayerInteractEvent`). Consomme de la durabilité et vérifie les permissions WorldGuard.

### 13. Semoir (`planter`)
*Plante les graines de la main gauche ou de la hotbar en zone.*
* **ID :** `planter`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5012`) — *&6Placard de Forge : Semoir*
* **Ingrédient (Slot 3) :** `DISPENSER` — *&aNoyau de Dispersion*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Zone **3x3** (Rayon 1)
  - **II :** Coût **2 slots**, Outil requis **Niv. 10**, Zone **5x5** (Rayon 2)
  - **III :** Coût **4 slots**, Outil requis **Niv. 15**, Zone **7x7** (Rayon 3)
* **Logique :** Clic droit sur la terre labourée avec une graine. Plante en zone en consommant graines et durabilité de l'outil.

### 14. Écorceur en Chaîne (`chain_stripper`)
*Écorce plusieurs bûches connectées d'un seul clic.*
* **ID :** `chain_stripper`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5013`) — *&6Placard de Forge : Écorceur en Chaîne*
* **Ingrédient (Slot 3) :** `SHEARS` — *&aNoyau de Coupe*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Max **10 bûches** écorcées
  - **II :** Coût **2 slots**, Outil requis **Niv. 10**, Max **32 bûches** écorcées
  - **III :** Coût **3 slots**, Outil requis **Niv. 15**, Max **64 bûches** écorcées
* **Logique :** Clic droit sur une bûche avec une hache. Écorce récursivement en chaîne dans la limite de blocs.

### 15. Moissonneur (`harvester`)
*Casse toutes les cultures mûres en zone.*
* **ID :** `harvester`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5014`) — *&6Placard de Forge : Moissonneur*
* **Ingrédient (Slot 3) :** `BONE_MEAL` — *&aNoyau de Croissance*
* **Niveaux :**
  - **I :** Coût **1 slot**, Outil requis **Niv. 5**, Zone **3x3** (Rayon 1)
  - **II :** Coût **2 slots**, Outil requis **Niv. 10**, Zone **5x5** (Rayon 2)
  - **III :** Coût **4 slots**, Outil requis **Niv. 15**, Zone **7x7** (Rayon 3)
* **Logique :** Intercepte le cassage d'une plante. Propage la casse aux cultures mûres adjacentes.

### 16. Indestructible (`unbreakable`)
*Rend l'outil insensible à l'usure de durabilité.*
* **ID :** `unbreakable`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5015`) — *&6Placard de Forge : Indestructible*
* **Ingrédient (Slot 3) :** `NETHER_STAR` — *&bÉtoile d'Indestructibilité*
* **Niveaux :**
  - **I :** Coût **4 slots**, Outil requis **Niv. 20**, Immunité complète
* **Logique :** Applique le tag Minecraft natif `Unbreakable` sur l'item via son ItemMeta.

### 17. Rebond (`bouncy`)
*Annule les dégâts de chute et propulse le joueur.*
* **ID :** `bouncy`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5016`) — *&6Placard de Forge : Rebond*
* **Ingrédient (Slot 3) :** `SLIME_BLOCK` — *&aNoyau Élastique*
* **Niveaux :**
  - **I :** Coût **1 slot**, Équipement requis **Niv. 5**, Rebondit de **30%** de la hauteur
  - **II :** Coût **2 slots**, Équipement requis **Niv. 10**, Rebondit de **50%** de la hauteur
  - **III :** Coût **4 slots**, Équipement requis **Niv. 15**, Rebondit de **80%** de la hauteur
* **Logique :** Intercepte les dégâts de chute. Calcule et applique un vecteur de vélocité vertical inversé. Réinitialise la distance de chute du joueur.

### 18. Purificateur (`purify`)
*Convertit les zombies villageois, piglins zombifiés ou sorcières en les frappant.*
* **ID :** `purify`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5017`) — *&6Placard de Forge : Purificateur*
* **Ingrédient (Slot 3) :** `GOLDEN_APPLE` — *&eNoyau de Purification*
* **Niveaux :**
  - **I :** Coût **1 slot**, Épée requise **Niv. 5**, **33%** chance de conversion par coup
  - **II :** Coût **2 slots**, Épée requise **Niv. 10**, **66%** chance de conversion par coup
  - **III :** Coût **3 slots**, Épée requise **Niv. 15**, **100%** chance de conversion par coup
* **Logique :** Écoute `EntityDamageByEntityEvent`. Si réussite, annule les dégâts et remplace ou convertit la créature (les sorcières deviennent des marchands ambulants, les piglins des piglins normaux immunisés). Consomme de la durabilité.

### 19. Pas de Plume (`feather_step`)
*Empêche d'activer les plaques, de piétiner ou de faire du bruit.*
* **ID :** `feather_step`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5019`) — *&6Placard de Forge : Pas de Plume*
* **Ingrédient (Slot 3) :** `FEATHER` — *&fPlume Céleste*
* **Niveaux :**
  - **I :** Coût **1 slot**, Bottes requises **Niv. 5**, Protège farmland et œufs de tortue
  - **II :** Coût **2 slots**, Bottes requises **Niv. 10**, Immunité aux plaques de pression et fils
  - **III :** Coût **3 slots**, Bottes requises **Niv. 15**, Silence absolu (annule sculk/warden vibrations)
* **Logique :** Intercepte les interactions physiques au sol et annule les vibrations sonores associées aux mouvements.

### 20. Résonance (`resonance`)
*Partage l'XP gagnée d'un outil avec vos pièces d'armure.*
* **ID :** `resonance`
* **Template (Slot 1) :** `NETHERITE_UPGRADE_SMITHING_TEMPLATE` (CMD: `5002`) — *&6Placard de Forge : Résonance*
* **Ingrédient (Slot 3) :** `QUARTZ` — *&fNoyau Résonant*
* **Niveaux :**
  - **I :** Coût **1 slot**, Armure requise **Niv. 5**, Partage **+10%** de l'XP de l'outil
  - **II :** Coût **2 slots**, Armure requise **Niv. 10**, Partage **+20%** de l'XP de l'outil
  - **III :** Coût **3 slots**, Armure requise **Niv. 15**, Partage **+35%** de l'XP de l'outil
* **Logique :** Si le joueur tient en main principale un outil qui gagne de l'XP, cette XP est distribuée en bonus à chaque pièce d'armure équipée possédant Résonance.
