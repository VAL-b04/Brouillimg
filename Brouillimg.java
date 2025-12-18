import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Brouillimg
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 3)
        {
            System.err.println("Usage: java Brouillimg <image_claire> <clé> [image_sortie] <process>");
            System.exit(1);
        }
        String inPath = args[0];
        String outPath = (args.length >= 3) ? args[2] : "out.png";
        // Masque 0x7FFF pour garantir que la clé ne dépasse pas les 15 bits
        int key = Integer.parseInt(args[1]) & 0x7FFF ;
        int process = Integer.parseInt(args[3]);

        BufferedImage inputImage = ImageIO.read(new File(inPath));
        if (inputImage == null)
        {
            throw new IOException("Format d’image non reconnu: " + inPath);
        }

        final int height = inputImage.getHeight();
        final int width = inputImage.getWidth();
        System.out.println("Dimensions de l'image : " + width + "x" + height);

        // Pré‑calcul des lignes en niveaux de gris pour accélérer le calcul du critère
        int[][] inputImageGL = rgb2gl(inputImage);

        int[] perm = generatePermutation(height, key);

        if (process == 0)
        {
            BufferedImage scrambledImage = scrambleLines(inputImage, perm);
            ImageIO.write(scrambledImage, "png", new File(outPath));
            System.out.println("Image écrite: " + outPath);
        }
        else
        {
            BufferedImage unScrambledImage = unScrambleLines(inputImage, perm);
            ImageIO.write(unScrambledImage, "png", new File(outPath));
            System.out.println("Image déchifrée: " + outPath);
        }
    }

    /**
     * Affiche un tableau de int
     * @param arr tableau à afficher
     */
    public static void printArray(int[] arr)
    {
        System.out.println("[");
        for (int i = 0; i < arr.length; i++)
        {
            if (i == arr.length - 1)
            {
                System.out.print(arr[i] + " ");
            }
            else
            {
                System.out.print(arr[i] + "; ");
            }
        }
        System.out.println("]");
    }

    /**
     * Convertit une image RGB en niveaux de gris (GL).
     * @param inputRGB image d'entrée en RGB
     * @return tableau 2D des niveaux de gris (0-255)
     */
    public static int[][] rgb2gl(BufferedImage inputRGB)
    {
        final int height = inputRGB.getHeight();
        final int width = inputRGB.getWidth();
        int[][] outGL = new int[height][width];

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int argb = inputRGB.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                // luminance simple (évite float)
                int gray = (r * 299 + g * 587 + b * 114) / 1000;
                outGL[y][x] = gray;
            }
        }

        return outGL;
    }

    /**
     * Génère une permutation des entiers 0..size-1 en fonction d'une clé.
     * @param size taille de la permutation
     * @param key clé de génération (15 bits)
     * @return tableau de taille 'size' contenant une permutation des entiers 0..size-1
     */
    public static int[] generatePermutation(int size, int key)
    {
        int[] scrambleTable = new int[size];
        for (int i = 0; i < size; i++)
        {
            scrambleTable[i] = scrambledId(i, size, key);
        }
        return scrambleTable;
    }

    /**
     * Mélange les lignes d'une image selon une permutation donnée.
     * @param inputImg image d'entrée
     * @param perm permutation des lignes (taille = hauteur de l'image)
     * @return image de sortie avec les lignes mélangées
     */
    public static BufferedImage scrambleLines(BufferedImage inputImg, int[] perm)
    {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        if (perm.length != height)
        {
            throw new IllegalArgumentException("Taille d'image <> taille permutation");
        }

        // Affichage du tableau de permutation
        //System.out.print("Table de permutation générée :");
        //printArray(perm);

        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Copie des lignes selon la permutation
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                outputImg.setRGB(x, y, inputImg.getRGB(x, perm[y]));
            }
        }

        return outputImg;
    }

    /**
     * Reconstitue l'image originale en dé-mélangeant les lignes selon une permutation donnée.
     * @param inputImg image brouillée
     * @param perm permutation des lignes (taille = hauteur de l'image)
     * @return image de sortie avec les lignes remises dans l'ordre original
     */
    public static BufferedImage unScrambleLines(BufferedImage inputImg, int[] perm)
    {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();

        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                outputImg.setRGB(x, perm[y], inputImg.getRGB(x, y));
            }
        }

        return outputImg;
    }

    /**
     * Reconstitue l'image originale en dé-mélangeant les lignes selon une permutation donnée.
     * @param inputImg image brouillée en noi et blanc
     * @param perm permutation des lignes (taille = hauteur de l'image)
     * @return image de sortie en noir et blanc avec les lignes remises dans l'ordre original
     */
    public static int[][] unScrambleLinesGL(int[][] inputGL, int[] perm)
    {
        int height = inputGL.length;
        int width = inputGL[0].length;

        int[][] outputGL = new int[height][width];
        
        for (int y = 0; y < height; y++)
        {
            outputGL[perm[y]] = inputGL[y];
        }

        return outputGL;
    }

    /**
     * Renvoie la position de la ligne id dans l'image brouillée.
     * @param id  indice de la ligne dans l'image claire (0..size-1)
     * @param size nombre total de lignes dans l'image
     * @param key clé de brouillage (15 bits)
     * @return indice de la ligne dans l'image brouillée (0..size-1)
     */
    public static int scrambledId(int id, int size, int key)
    {
        int s = getSKey(key);
        int r = getRKey(key);
        return ((r + (2 * s + 1) * id) % size);
    }

    /**
     * Renvoie le paramètre s de la clé key
     * @param key clé de brouillage (15 bits)
     * @return paramètre s de la clé (7 bits)
     */
    public static int getSKey(int key)
    {
        // masque binaire pour récupérer les 7 derniers bits
        return key & 0x7F;
    }

    /**
     * Renvoie le paramètre r de la clé key
     * @param key clé de brouillage (15 bits)
     * @return paramètre r de la clé (8 bits)
     */
    public static int getRKey(int key)
    {
        // division par 2^7 pour décaler les 8 bits de r 7 fois vers la droite
        return key / 128;
    }

    // BRUTE FORCE
    /**
     * Calcule la distance euclidienne entre deux lignes de pixels d'une image.
     * @param imageGL matrice de l'image en noir et blanc
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la deuxième ligne
     * @return distance euclidienne moyenne entre les deux lignes
     */
    public static double euclideanDistance(int[][] imageGL, int ligne1, int ligne2)
    {
        double somme = 0.0;
        int width = imageGL[0].length;

        for (int i = 0; i < width; i++)
        {
            somme += Math.pow(imageGL[ligne1][i] - imageGL[ligne2][i], 2);
        }
        return Math.sqrt(somme);
    }

    /**
     * Calcule le score total d'une image en sommant les distances euclidiennes entre chaque paire de lignes consécutives.
     * @param imageGL matrice de l'image en noir et blanc
     * @return score euclidien total
     */
    public static double scoreEuclidean(int[][] imageGL)
    {
        double score = 0.0;

        // Parcours toutes les paires de lignes consécutives
        for (int i = 0; i < imageGL.length - 1; i++)
        {
            score += euclideanDistance(imageGL, i, i + 1);
        }

        return score;
    }

    // CORRÉLATION DE PEARSON
    /**
     * Calcule le coefficient de corrélation de Pearson entre deux lignes de pixels d'une image.
     * @param imageGL matrice de l'image en noir et blanc
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la deuxième ligne
     * @return coefficient de corrélation (entre -1 et 1)
     */
    public static double pearsonCorrelation(int[][] imageGL, int ligne1, int ligne2)
    {
        int width = imageGL[0].length;
        double moyenneX = 0.0;
        double moyenneY = 0.0;

        // Calcul des moyennes de x et y
        for (int i = 0; i < width; i++)
        {
            moyenneX += imageGL[ligne1][i];
            moyenneY += imageGL[ligne2][i];
        }

        moyenneX /= width;
        moyenneY /= width;

        double numerateur = 0.0;
        double sommeX = 0.0;
        double sommeY = 0.0;

        // Calcul de la corrélation de Pearson
        for (int i = 0; i < width; i++)
        {
            double diffX = imageGL[ligne1][i] - moyenneX;
            double diffY = imageGL[ligne2][i] - moyenneY;

            numerateur += diffX * diffY;
            sommeX += diffX * diffX;
            sommeY += diffY * diffY;
        }

        // Calcul du dénominateur
        double denominateur = Math.sqrt(sommeX) * Math.sqrt(sommeY);

        // Éviter la division par zéro
        if (denominateur == 0)
        {
            return 0.0;
        }

        return numerateur / denominateur;
    }

    /**
     * Calcule le score total d'une image en sommant les corrélations de Pearson
     * entre chaque paire de lignes consécutives.
     * Plus le score est ÉLEVÉ, plus l'image est probablement correcte.
     * @param imageGL matrice de l'image en noir et blanc
     * @return score Pearson total
     */
    public static double scorePearson(int[][] imageGL)
    {
        double score = 0.0;
        int height = imageGL.length;

        // Parcours toutes les paires de lignes consécutives
        for (int i = 0; i < height - 1; i++)
        {
            score += pearsonCorrelation(imageGL, i, i + 1);
        }

        return score;
    }

    // METHODE MANHATTAN
    /**
     * Calcule la distance de Manhattan entre deux lignes de pixels d'une image.
     * La distance de Manhattan est la somme des valeurs absolues des différences.
     * @param imageGL matrice de l'image en noir et blanc
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la deuxième ligne
     * @return distance de Manhattan entre les deux lignes
     */
    public static double manhattanDistance(int[][] imageGL, int ligne1, int ligne2)
    {
        double somme = 0.0;
        int width = imageGL[0].length;

        for (int i = 0; i < width; i++)
        {
            somme += Math.abs(imageGL[ligne1][i] - imageGL[ligne2][i]);
        }
        return somme;
    }

    /**
     * Calcule le score total d'une image en sommant les distances de Manhattan
     * entre chaque paire de lignes consécutives.
     * Plus le score est FAIBLE, plus l'image est probablement correcte.
     * @param imageGL matrice de l'image en noir et blanc
     * @return score Manhattan total
     */
    public static double scoreManhattan(int[][] imageGL)
    {
        double score = 0.0;

        // Parcours toutes les paires de lignes consécutives
        for (int i = 0; i < imageGL.length - 1; i++)
        {
            score += manhattanDistance(imageGL, i, i + 1);
        }

        return score;
    }

    // CORRELATION CROISÉ NORMALISÉ
    /**
     * Calcule la corrélation croisée normalisée (NCC) entre deux lignes de pixels.
     * La NCC mesure la similarité entre deux signaux normalisés.
     * Retourne une valeur entre -1 et 1, où 1 indique une corrélation parfaite.
     * @param imageGL matrice de l'image en noir et blanc
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la deuxième ligne
     * @return coefficient de corrélation croisée normalisée (entre -1 et 1)
     */
    public static double normalizedCrossCorrelation(int[][] imageGL, int ligne1, int ligne2)
    {
        int width = imageGL[0].length;
        double sommeXY = 0.0;
        double sommeX2 = 0.0;
        double sommeY2 = 0.0;

        for (int i = 0; i < width; i++)
        {
            int x = imageGL[ligne1][i];
            int y = imageGL[ligne2][i];
            
            sommeXY += x * y;
            sommeX2 += x * x;
            sommeY2 += y * y;
        }

        double denominateur = Math.sqrt(sommeX2) * Math.sqrt(sommeY2);

        // Éviter la division par zéro
        if (denominateur == 0)
        {
            return 0.0;
        }

        return sommeXY / denominateur;
    }

    /**
     * Calcule le score total d'une image en sommant les corrélations croisées normalisées
     * entre chaque paire de lignes consécutives.
     * Plus le score est ÉLEVÉ, plus l'image est probablement correcte.
     * @param imageGL matrice de l'image en noir et blanc
     * @return score NCC total
     */
    public static double scoreNCC(int[][] imageGL)
    {
        double score = 0.0;
        int height = imageGL.length;

        // Parcours toutes les paires de lignes consécutives
        for (int i = 0; i < height - 1; i++)
        {
            score += normalizedCrossCorrelation(imageGL, i, i + 1);
        }

        return score;
    }

    // DIVERGENCE DE KULBACK-LEIBLER
    /**
     * Calcule la divergence de Kullback-Leibler (KL) entre deux lignes de pixels.
     * La divergence KL mesure la différence entre deux distributions de probabilités.
     * Plus la valeur est faible, plus les distributions sont similaires.
     * Note: les valeurs de pixels sont normalisées pour former des distributions de probabilités.
     * @param imageGL matrice de l'image en noir et blanc
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la deuxième ligne
     * @return divergence de Kullback-Leibler (toujours >= 0)
     */
    public static double kullbackLeiblerDivergence(int[][] imageGL, int ligne1, int ligne2)
    {
        int width = imageGL[0].length;
        double epsilon = 1e-10; // Pour éviter log(0)
        
        // Normalisation des lignes en distributions de probabilités
        double[] p = new double[width];
        double[] q = new double[width];
        double sommeP = 0.0;
        double sommeQ = 0.0;

        // Calcul des sommes pour normalisation
        for (int i = 0; i < width; i++)
        {
            p[i] = imageGL[ligne1][i] + epsilon;
            q[i] = imageGL[ligne2][i] + epsilon;
            sommeP += p[i];
            sommeQ += q[i];
        }

        // Normalisation
        for (int i = 0; i < width; i++)
        {
            p[i] /= sommeP;
            q[i] /= sommeQ;
        }

        // Calcul de la divergence KL: KL(P||Q) = sum(P(i) * log(P(i)/Q(i)))
        double divergence = 0.0;
        for (int i = 0; i < width; i++)
        {
            divergence += p[i] * Math.log(p[i] / q[i]);
        }

        return divergence;
    }

    /**
     * Calcule le score total d'une image en sommant les divergences de Kullback-Leibler
     * entre chaque paire de lignes consécutives.
     * Plus le score est FAIBLE, plus l'image est probablement correcte.
     * @param imageGL matrice de l'image en noir et blanc
     * @return score KL total
     */
    public static double scoreKullbackLeibler(int[][] imageGL)
    {
        double score = 0.0;
        int height = imageGL.length;

        // Parcours toutes les paires de lignes consécutives
        for (int i = 0; i < height - 1; i++)
        {
            score += kullbackLeiblerDivergence(imageGL, i, i + 1);
        }

        return score;
    }

    // BREAKKEY
    /**
     * Teste toutes les clés possibles pour identifier la clé qui produit l'image la plus cohérente.
     * @param scrambledImage l'image brouillée à déchiffrer
     * @param methodeType type de score à utiliser : "Euclide", "Manhattan", "Pearson", "NCC" ou "KL"
     * @return la clé qui donne le meilleur résultat
     */
    public static int breakKey(BufferedImage scrambledImage, String methodeType)
    {
        int height = scrambledImage.getHeight();

        int bestKey = -1;
        double bestScore;

        // Initialisation du score selon la méthode
        switch (methodeType.toLowerCase())
        {
            case "pearson":
            case "ncc":
                // Pour les méthodes de corrélation : on cherche le score MAX
                bestScore = Double.MIN_VALUE;
                break;
            case "euclide":
            case "manhattan":
            case "kl":
            default:
                // Pour les méthodes de distance : on cherche le score MIN
                bestScore = Double.MAX_VALUE;
                break;
        }

        double[][] top10Key = new double[10][2];

        // Initialise le tableau top10
        for (int i = 0; i < top10Key.length; i++)
        {
            top10Key[i][0] = bestScore;
            top10Key[i][1] = -1;
        }

        System.out.println("Recherche de la clé avec le critère : " + methodeType);

        // Teste toutes les clés possibles (2^15 = 32768)
        for (int key = 0; key < 32768; key++)
        {
            System.out.println(key);

            // Génère la permutation avec cette clé
            int[] perm = generatePermutation(height, key);

            // Déchiffre l'image avec cette clé
            BufferedImage unscrambledImage = unScrambleLines(scrambledImage, perm);

            // Convertit en niveaux de gris
            int[][] imageGL = rgb2gl(unscrambledImage);

            // Calcule le score selon le type choisi
            double score;

            switch (methodeType.toLowerCase())
            {
                case "euclide":
                    score = scoreEuclidean(imageGL);
                    break;
                case "manhattan":
                    score = scoreManhattan(imageGL);
                    break;
                case "pearson":
                    score = scorePearson(imageGL);
                    break;
                case "ncc":
                    score = scoreNCC(imageGL);
                    break;
                case "kl":
                    score = scoreKullbackLeibler(imageGL);
                    break;
                default:
                    score = scoreEuclidean(imageGL);
                    break;
            }

            // Mise à jour du meilleur score selon la méthode
            boolean isBetter = false;
            switch (methodeType.toLowerCase())
            {
                case "pearson":
                case "ncc":
                    // Pour les corrélations, un score plus élevé est meilleur
                    isBetter = score > bestScore;
                    break;
                case "euclide":
                case "manhattan":
                case "kl":
                default:
                    // Pour les distances, un score plus faible est meilleur
                    isBetter = score < bestScore;
                    break;
            }

            if (isBetter)
            {
                bestScore = score;
                bestKey = key;
            }

            // Classement des 10 meilleures clés
            for (int i = 0; i < 0; i++)
            {
                // Décale les éléments
                for (int j = top10Key.length - 1; j > i; j--)
                {
                    top10Key[j][0] = top10Key[j - 1][0];
                    top10Key[j][1] = top10Key[j - 1][1];
                }
                top10Key[i][0] = score;
                top10Key[i][1] = key;
                break;
            }
        }

        // Affiche le résultat final avec les bonnes valeurs
        System.out.println("La méthode : " + methodeType + " avec la clé " + bestKey + " a obtenu un score de " + bestScore + "\n");

        System.out.println("Top 10 des clés : ");

        for (int i = 0; i < top10Key.length; i++)
        {
            if (top10Key[i][1] != -1)
            {
                System.out.println("Position " + (i+1) + " : Clé " + (int)top10Key[i][1] + " | Score : " + top10Key[i][0]);
            }
        }

        return bestKey;
    }

    /**
     * Retourne deux valeurs possibles pour le paramètre s de la clé.
     * Explication à détailler en soutenance
     * @param imageGl image mélangée en noir et blanc
     * @return deux valeurs de s possibles de la clé à trouver
     */
    public static int[] getSsKeyFromImage(int[][] imageGL)
    {
        final int height = imageGL.length;

        // On utilisera le crière de la distance euclidienne, donc on par du MAX vers le MIN
        double bestScore = Double.MAX_VALUE-1;
        double secondBestScore = Double.MAX_VALUE;

        // On associe chaque score avec leurs lignes respectives
        int bestLine = 1;
        int secondBestLine = 1;

        // Recherche des deux lignes les plus similaires à la première (celle qui suit et celle qui précède)
        for (int line = 1; line < height; line++)
        {
            double score = euclideanDistance(imageGL, 0, line);
            if (score < secondBestScore)
            {
                if (score < bestScore)
                {
                    secondBestScore = bestScore;
                    secondBestLine = bestLine;

                    bestScore = score;
                    bestLine = line;
                }
                else
                {
                    secondBestScore = score;
                    secondBestLine = line;
                }
            }
        }

        // Détermine les valeurs de s selon les lignes trouvées (explication à détailler ...)
        int x1 = 0;
        while (x1 < height && (x1*bestLine)%height != 1)
        {
            x1++;
        }
        int x2 = 0;
        while (x2 < height && (x2*secondBestLine)%height != 1)
        {
            x2++;
        }
        int[] ss = {(x1-1)/2, (x2-1)/2};

        return ss;
    }

    /**
     * Retourne la valeur de r pour la clé à trouver, en sachant que le s est connu.
     * Explication à détailler en soutenance
     * @param imageGL l'image avec les lignes agencées (s déjà appliqué) mais décalée
     * @return la valeur de r pour que l'image soit décryptée correctement
     */
    public static int getRKeyFromImage(int[][] imageGL) {
        int n = imageGL.length;
        double worstScore = Double.MIN_VALUE;
        int worstLine = 0;
        double score = 0;

        for (int i = 0; i < n-1; i++) {
            score = euclideanDistance(imageGL, i, i+1);
            if (score > worstScore)
            {
                worstScore = score;
                worstLine = i;
            }
        }

        score = euclideanDistance(imageGL, 0, n-1);
        if (score > worstScore)
        {
            worstLine = n-1;
        }

        System.out.println(worstLine);
        if (worstLine < 256)
        {
            return 256 - worstLine;
        }
        return n - worstLine - 1;
    }

    /**
     * Version plus "mathématique" de breakKey, qui ne regarde que 2 clés
     * @param scrambledImage image mélangée à décrypter
     * @param methodeType type de score à utiliser : "Euclide" ou "Pearson"
     * @return la clé qui donne le meilleur résultat
     */
    public static int breakKey2(BufferedImage scrambledImage, String methodeType)
    {
        int height = scrambledImage.getHeight();
        int width = scrambledImage.getWidth();

        int bestKey = -1;
        double bestScore;

        // Pour Euclide : on cherche le score MIN (distance faible)
        // Pour Pearson : on cherche le score MAX (corrélation élevée)
        if (methodeType.equalsIgnoreCase("Euclide"))
        {
            bestScore = Double.MAX_VALUE;  // MAX pour chercher le MIN
        }
        else // Pearson
        {
            bestScore = Double.MIN_VALUE;  // MIN pour chercher le MAX
        }

        // Convertit en niveaux de gris
        int[][] imageGL = rgb2gl(scrambledImage);

        int[][] unscrambledImage = new int[height][width];

        // Recherche des deux valeurs possibles pour le paramètre s de la clé
        int[] ss = getSsKeyFromImage(imageGL);

        System.out.println("Recherche de la clé avec le critère : " + methodeType);

        // Teste les deux valeurs de s (2*256)
        for (int s : ss)
        {
            // Génère la permutation avec ce s
            int[] perm = generatePermutation(height, s);

            // Déchiffre l'image avec cette clé
            unscrambledImage = unScrambleLinesGL(imageGL, perm);

            // Calcule le score selon le type choisi
            double score;

            // Appelle de la méthode appropriée
            if (methodeType.equalsIgnoreCase("Euclide"))
            {
                score = scoreEuclidean(unscrambledImage);
                if (score < bestScore)
                {
                    bestScore = score;
                    bestKey = s;
                }
            }
            else  // Pearson
            {
                score = scorePearson(unscrambledImage);
                if (score > bestScore)
                {
                    bestScore = score;
                    bestKey = s;
                }
            }
        }

        // Regénère la permutation avec le meilleur s
        int[] perm = generatePermutation(height, bestKey);

        // Déchiffre l'image avec ce s
        unscrambledImage = unScrambleLinesGL(imageGL, perm);

        System.out.println(bestKey);
        int r = getRKeyFromImage(unscrambledImage);
        bestKey += 128*r;

        // Affiche le résultat final avec les bonnes valeurs
        System.out.println("La méthode : " + methodeType + " a trouvé comme clé : " + bestKey);

        return bestKey;
    }

}