import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class BrouillimgTest
{

    public static void main(String[] args) throws IOException
    {
        /*
        testScrambleLines(19504);
        testUnScrambleLines(48, "out2.png");
        testUnScrambleLines(48+45*128, "out3.png");
        testUnScrambleLines(48+70*128, "out4.png");
        testUnScrambleLines(48+128*128, "out5.png");
        testUnScrambleLines(48+170*128, "out6.png");
        testUnScrambleLines(48+210*128, "out7.png");
        testUnScrambleLines(48+255*128, "out8.png");
        */
        //testUnScrambleLines(53249, "out2.png");
        
        testBreakKey();
        //testBreakKey2();
    }

    public static void printArray(int[] arr)
    {
		System.out.print("[ ");
		for (int i = 0; i < arr.length; i++)
		{
		    System.out.print(arr[i] + "; ");
		}
		System.out.println("]");
    }

    public static void printArrayByte(byte[] arr)
    {
		System.out.print("[ ");
		for (int i = 0; i < arr.length; i++)
		{
		    System.out.print(arr[i] + "; ");
		}
		System.out.println("]");
    }

    public static void testGetRSKey()
    {
		System.out.println(1000);
		System.out.println(Brouillimg.getRKey(1000));
		System.out.println(Brouillimg.getSKey(1000));
    }

    public static void testGeneratePermutation()
    {
		printArray(Brouillimg.generatePermutation(8, 129));
    }

    public static void testScrambleLines(int key) throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/staline.jpg"));
        int[] perm = Brouillimg.generatePermutation(1024, key);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        ImageIO.write(scrambledImage, "png", new File("out.png"));
    }

    public static void testUnScrambleLines(int key, String path) throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("out.png"));
        int[] perm = Brouillimg.generatePermutation(1024, key);
        BufferedImage scrambledImage = Brouillimg.unScrambleLines(inputImage, perm);
        ImageIO.write(scrambledImage, "png", new File(path));
    }

    public static void testEuclidian() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/arc-en-ciel-traits.jpg"));
        int[] perm = Brouillimg.generatePermutation(1024, 1000);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        System.out.println(Brouillimg.scoreEuclidean(Brouillimg.rgb2gl(scrambledImage)));  
        System.out.println(Brouillimg.scoreEuclidean(Brouillimg.rgb2gl(inputImage)));  
    }

    public static void testBreakKey() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/arc_en_ciel_trait.jpg"));
        int[] perm = Brouillimg.generatePermutation(512, 11114);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        Brouillimg.breakKey(scrambledImage, "Euclide");
    }

    public static void testBreakKey2() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/arc_en_ciel_trait.jpg"));
        int[] perm = Brouillimg.generatePermutation(512, 11114);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        Brouillimg.breakKey2(scrambledImage, "Euclide");
    }

    public static void testGetSKeyImage() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("out.png"));
        int[][] imageGL = Brouillimg.rgb2gl(inputImage);
        int[] ss = Brouillimg.getSsKeyFromImage(imageGL);
        System.out.println(ss[0]);
        System.out.println(ss[1]);
    }

    public static void testIsNumber()
    {
        System.out.println(Brouillimg.isNumber("49130"));
        System.out.println(Brouillimg.isNumber("4ab30"));
        System.out.println(Brouillimg.isNumber(""));
    }
}