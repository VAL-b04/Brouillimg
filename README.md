# Brouillimg - Image Line Scrambler

Un **système de chiffrement d'images** développé en **Java**, utilisant une technique de permutation de lignes pour brouiller et déchiffrer des images. Le projet inclut également des algorithmes de **cryptanalyse** pour retrouver la clé de chiffrement sans la connaître.

---

## Principe du chiffrement

Le système applique une permutation déterministe des lignes d'une image en fonction d'une clé de 15 bits :

### Formule de chiffrement
Pour une image de hauteur `height` (puissance de 2), chaque ligne d'indice `idLigne` est déplacée vers la position :
```
position_chiffrée = (r + (2s + 1) × idLigne) % height
```

Où :
- **r** = décalage (offset) codé sur **8 bits** (0-255)
- **s** = pas (step) codé sur **7 bits** (0-127)
- **Clé totale** = 15 bits = `(r << 7) | s`

---

## Fonctionnalités

### 1. **Chiffrement (Brouiller)**
- Applique la permutation des lignes selon la clé fournie
- Génère une image brouillée

### 2. **Déchiffrement (Débrouiller)**
- Applique la transformation inverse avec la même clé
- Restaure l'image originale/débrouille l'image

### 3. **Force brute - breakKey()**
**Principe** : Teste **toutes les clés possibles** soit 32 768 clès.

**Algorithme** :
```java
Pour chaque clé de 0 à 32767 :
    1. Générer la permutation avec cette clé
    2. Déchiffrer l'image
    3. Calculer le score de similarité entre lignes consécutives
    4. Conserver la clé donnant le meilleur score
```

**Critères de similarité** :

#### **Critères de distance** (score minimal = meilleur) :

- **Euclidienne** : Mesure la distance géométrique classique entre deux lignes. Calcule la racine carrée de la somme des différences au carré. Simple et efficace, mais sensible aux variations d'éclairage.

- **Manhattan** : Somme des différences absolues pixel par pixel. Plus robuste aux valeurs extrêmes que l'euclidienne, calcul plus rapide.

- **Kullback-Leibler (KL)** : Mesure la divergence entre deux distributions de probabilités. Nécessite de normaliser les lignes. Très sensible aux petites variations, bon pour détecter des différences subtiles.

#### **Critères de corrélation** (score maximal = meilleur) :

- **Pearson** : Mesure la corrélation linéaire en tenant compte des moyennes. Normalisé entre -1 et 1. Robuste aux variations d'éclairage car compare les écarts à la moyenne.

- **NCC (Normalized Cross-Correlation)** : Corrélation croisée normalisée sans centrage sur la moyenne. Mesure la similarité directe entre signaux. Plus simple que Pearson, bon pour images à contraste stable.

### 4. **Cryptanalyse optimisée - breakKey2()**
**Principe** : Approche **mathématique** qui réduit drastiquement l'espace de recherche.

**Algorithme en 3 étapes** :

#### **Étape 1 : Déterminer les valeurs candidates de `s`**
```java
1. Identifier les 2 lignes les plus similaires à la ligne 0
   → Ces lignes correspondent aux lignes qui suivent/précèdent 
     la ligne 0 dans l'image originale
2. Calculer leurs positions (bestLine, secondBestLine)
3. Résoudre : (x × bestLine) mod height = 1
   → Déduire s₁ = (x-1)/2
4. Faire de même pour secondBestLine → s₂
```
**Résultat** : 2 valeurs candidates pour `s` au lieu de 128.

#### **Étape 2 : Tester uniquement les 2 valeurs de `s`**
```java
Pour chaque s dans {s₁, s₂} :
    1. Générer la permutation avec s (r=0 temporairement)
    2. Déchiffrer partiellement l'image
    3. Calculer le score
    4. Conserver le meilleur s
```

#### **Étape 3 : Déterminer `r` à partir du meilleur `s`**
```java
1. Avec le s optimal, l'image est correctement ordonnée mais décalée
2. Trouver la "coupure" : la paire de lignes consécutives 
   avec la plus grande distance
3. Cette position indique le décalage r
4. Calculer : r = (height - position_coupure - 1) ou (256 - position_coupure)
```

**Gain de performance** :
- **breakKey()** : teste 32 768 clés
- **breakKey2()** : teste ~256 clés (2 valeurs de `s` × ~128 positions pour `r`)
- **Accélération** : **~64 fois plus rapide** 🚀

---

## Interface graphique

Interface développée avec **StdDraw** proposant :
- **4 champs de texte** :
  - Chemin d'entrée de l'image
  - Chemin de sortie de l'image
  - Clé de chiffrement
  - Méthode de cryptanalyse (euclide, manhattan, pearson, ncc, kl)
- **4 boutons** :
  - **Brouiller** : chiffre l'image avec la clé fournie
  - **Débrouiller** : déchiffre l'image avec la clé fournie
  - **Casser la clé** : utilise `breakKey()` (force brute)
  - **Casser la clé (2)** : utilise `breakKey2()` (optimisé)
- **Retour visuel** : messages de succès/erreur affichés en rouge

---

## Installation et utilisation

```bash
# Cloner le dépôt
git clone https://github.com/VAL-b04/Brouillimg
cd Brouillimg

# Compiler les fichiers
javac StdDraw.java Brouillimg.java

# Lancer l'interface graphique
java Brouillimg
```

### Utilisation via l'interface

**Chiffrer une image** :
1. Entrer le chemin de l'image source (ex: `image.png`)
2. Entrer le chemin de sortie souhaité (ex: `brouille.png`)
3. Entrer une clé (0-32767)
4. Cliquer sur "Brouiller"

**Déchiffrer une image** :
1. Entrer le chemin de l'image brouillée
2. Entrer la clé utilisée lors du chiffrement
3. Cliquer sur "Débrouiller"

**Retrouver une clé inconnue** :
1. Entrer le chemin de l'image brouillée
2. Choisir une méthode dans le champ "Methode" :
   - `euclide` : distance euclidienne
   - `manhattan` : distance de Manhattan
   - `pearson` : corrélation de Pearson
   - `ncc` : corrélation croisée normalisée
   - `kl` : divergence de Kullback-Leibler
3. **Option 1** : Cliquer sur "Casser la clé" → `breakKey()` (force brute, lent mais exhaustif)
4. **Option 2** : Cliquer sur "Casser la clé (2)" → `breakKey2()` (rapide et intelligent)
