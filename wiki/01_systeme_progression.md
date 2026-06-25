# 📈 Système de Progression (XP & Niveaux) — DanaTools

Ce guide détaille la mécanique de progression des équipements évolutifs de **DanaTools**, incluant les formules de calcul d'XP, les sources de gain passif et actif, ainsi que le système de synergie par résonance.

---

## 1. La Courbe d'XP

Chaque outil ou armure évolutive possède une courbe d'XP configurée dans son fichier YAML (situé sous `tools/`). Elle définit la quantité d'XP requise pour qu'un équipement passe au niveau supérieur.

### La Formule Mathematique
L'XP nécessaire pour passer du niveau actuel $N$ au niveau supérieur $N + 1$ est régie par la formule suivante :

$$\text{XP requis} = \text{arrondir}\left(\text{Base} \times N^{\text{Multiplicateur}}\right)$$

Où :
* **$\text{Base}$** : La valeur d'XP de base configurée sous `xp-curve.base`.
* **$\text{Multiplicateur}$** : Le coefficient d'accroissement de la difficulté configuré sous `xp-curve.multiplier`.

### Exemple Théorique
Pour une configuration type avec une base de `100` et un multiplicateur de `1.5` :

| Niveau Actuel ($N$) | Formule | XP requis pour $N+1$ | XP Cumulée |
| :---: | :--- | :---: | :---: |
| **1** | $100 \times 1^{1.5}$ | **100** | 100 |
| **2** | $100 \times 2^{1.5}$ | **283** | 383 |
| **3** | $100 \times 3^{1.5}$ | **520** | 903 |
| **4** | $100 \times 4^{1.5}$ | **800** | 1 703 |
| **5** | $100 \times 5^{1.5}$ | **1 118** | 2 821 |
| **10** | $100 \times 10^{1.5}$ | **3 162** | 17 568 |

*Note : Dès que l'équipement atteint son niveau maximal (`max-level`), son XP est bloquée à 0.*

---

## 2. Progression des Outils (XP Passive)

Les outils (pioches, haches, pelles, houes) gagnent de l'XP de manière passive en effectuant leurs tâches principales.

### Activités et Blocs compatibles
Le gain d'XP dépend de la section `block-activities` (ou de l'ancien système `xp-gain` en rétrocompatibilité) définie dans le fichier YAML de l'outil.
* **Minage :** Destinés aux pioches et pelles (ex: casser du diamant rapporte 10 XP, du charbon 2 XP).
* **Coupe de bois :** Haches (ex: bûches, feuilles).
* **Agriculture :** Houes (ex: récolter des cultures arrivées à maturité).
* **Pêche :** Les cannes à pêche gagnent de l'XP en ferrant et attrapant un poisson, configurée sous la section `fishing-activity`.

### Boost d'Apprentissage (`learning`)
Si le joueur possède le modificateur **Apprentissage** (`learning`) équipé, l'XP accumulée par l'outil en main est augmentée d'un certain pourcentage :
- **Apprentissage I :** +20% d'XP
- **Apprentissage II :** +40% d'XP
- **Apprentissage III :** +60% d'XP

---

## 3. Progression des Armures (XP Active)

Les pièces d'armure évolutives progressent via deux sources d'XP actives basées sur les actions directes du joueur lorsqu'elles sont équipées dans les slots d'armure (Casque, Plastron, Jambières, Bottes).

### A. XP de Combat
Chaque fois que le joueur subit des dégâts d'une entité ennemie (monstre ou autre joueur), l'armure absorbe le choc et gagne de l'XP.
* **Calcul :** $\text{XP} = \text{Dégâts Finaux} \times \text{xp-gain-damage-multiplier}$.
* **Restrictions :** 
  - Sont exclus tous les dégâts environnementaux (chute, noyade, feu, suffocation, vide).
  - Sont exclus les dégâts auto-infligés (flèches tirées par soi-même, potions jetées par soi-même).

### B. XP d'Exploration
Le simple fait de parcourir le monde à pied fait progresser vos pièces d'armures.
* **Calcul :** $\text{XP} = \text{Distance parcourue} \times \text{xp-gain-movement-multiplier}$.
* **Fonctionnement :**
  - Un scheduler vérifie la position de tous les joueurs en ligne toutes les **5 secondes (100 ticks)**.
  - Si la distance parcourue à pied depuis la dernière mesure est supérieure à 2 blocs et inférieure à 50 blocs, l'XP est distribuée.
  - Sont exclues les distances parcourues en monture/véhicule (cheval, minecart, bateau) ou en volant (élytres).

---

## 4. Les Paliers de Level-Up

Lorsqu'un outil ou une armure franchit un palier d'XP requis :
1. **Montée de Niveau :** Le niveau de l'item augmente de 1.
2. **Notification visuelle et sonore :**
   - Un son angélique de montée de niveau est joué au joueur (`ENTITY_PLAYER_LEVELUP`).
   - Des particules vertes de villageois joyeux (`HAPPY_VILLAGER`) jaillissent autour de lui.
   - Un message de félicitations s'affiche dans le chat (configuré dans [lang/fr.yml](https://github.com/DanaKube-Mc/DanaTools/blob/main/src/main/resources/lang/fr.yml)).
3. **Mise à jour des Slots de Modificateurs :**
   - Le plugin lit la section `progression.slots` pour définir le nombre de slots totaux débloqués à ce nouveau niveau.
   - Exemple : 1 slot à niveau 5, 2 slots à niveau 10, etc.

---

## 5. Le Modificateur Résonance (`resonance`)

La résonance est une liaison d'énergie spirituelle entre vos outils et vos pièces d'armure.

* **Comportement :** Lorsqu'un outil tenu dans la main principale du joueur gagne de l'XP (via le minage ou la récolte), il partage de manière bonus un pourcentage de cette XP avec chaque pièce d'armure évolutive équipée possédant le modificateur `resonance`.
* **Partage d'XP (selon le niveau de Résonance sur la pièce d'armure) :**
  - **Résonance I :** **+10%** de l'XP de l'outil.
  - **Résonance II :** **+20%** de l'XP de l'outil.
  - **Résonance III :** **+35%** de l'XP de l'outil.

### Exemple de Résonance :
Si vous gagnez 100 XP sur votre pioche principale en minant un minerai et que vous portez un plastron avec *Résonance II* et des bottes avec *Résonance III* :
- La pioche reçoit **100 XP** (+ boost *learning* s'il y a lieu).
- Le plastron reçoit **20 XP** bonus.
- Les bottes reçoivent **35 XP** bonus.
