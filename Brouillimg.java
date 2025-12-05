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
        System.out.print("Table de permutation générée :");
        printArray(perm);

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

    /**
     * Calcule la distance euclidienne entre deux lignes de pixels d'une image.
     * @param imageGL matrice de l'image en noir et blanc
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la deuxième ligne
     * @return distance euclidienne moyenne entre les deux lignes
     */
    public static double euclideanDistance(int[][] imageGL, int ligne1, int ligne2)
    {
        double somme = 0;
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

    /**
     * Teste toutes les clés possibles pour identifier la clé qui produit l'image la plus cohérente.
     * @param scrambledImage l'image brouillée à déchiffrer
     * @return la clé qui donne le meilleur résultat
     */
    public static int breakKey(BufferedImage scrambledImage)
    {
        int height = scrambledImage.getHeight();
        double bestScore = Double.MAX_VALUE; // Valeur maximale
        int bestKey = 0;
        double[][] top10Key = new double[10][2];

        // Teste toutes les clés possibles (2^15 = 32768)
        for (int key = 0; key < 32768; key++)
        {
            // Génère la permutation avec cette clé
            int[] perm = generatePermutation(height, key);

            // Déchiffre l'image avec cette clé
            BufferedImage unscrambledImage = unScrambleLines(scrambledImage, perm);

            // Convertie en niveaux de gris
            int[][] imageGL = rgb2gl(unscrambledImage);

            // Calcul le score euclidien
            double score = scoreEuclidean(imageGL);

            // Garde la meilleure clé
            if (score < bestScore)
            {
                bestScore = score;
                bestKey = key;
            }

            // Classement des 10 meilleures clés
            for (int i = 0; i < top10Key.length; i++)
            {
                if (score < top10Key[i][0])
                {
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
        }

        // Affiche le résultat final
        System.out.println("\nClé trouvée: " + bestKey + " avec un score de : " + bestScore);

        System.out.println("Affichage des 10 meilleurs clès ainsi que leur score\n");
        for (int i = 0; i < top10Key.length; i++)
        {
            if (top10Key[i][1] != -1)  // Vérifie qu'une clé valide existe
            {
                System.out.println("Position " + (i+1) + " : Clé " + (int)top10Key[i][1] + " | Score : " + top10Key[i][0]);
            }
        }

        return bestKey;
    }
}