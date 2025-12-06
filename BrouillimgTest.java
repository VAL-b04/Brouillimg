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
        testScrambleLines();
        testBreakKey();
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

    public static void testScrambleLines() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/staline.jpg"));
        int[] perm = Brouillimg.generatePermutation(1024, 1000);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        ImageIO.write(scrambledImage, "png", new File("out.png"));
    }

    public static void testUnScrambleLines() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("out.png"));
        int[] perm = Brouillimg.generatePermutation(1024, 1000);
        BufferedImage scrambledImage = Brouillimg.unScrambleLines(inputImage, perm);
        ImageIO.write(scrambledImage, "png", new File("out2.png"));
    }

    public static void testEuclidian() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/staline.jpg"));
        int[] perm = Brouillimg.generatePermutation(1024, 1000);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        System.out.println(Brouillimg.scoreEuclidean(Brouillimg.rgb2gl(scrambledImage)));  
        System.out.println(Brouillimg.scoreEuclidean(Brouillimg.rgb2gl(inputImage)));  
    }

    public static void testBreakKey() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("images/staline.jpg"));
        int[] perm = Brouillimg.generatePermutation(1024, 19504);
        BufferedImage scrambledImage = Brouillimg.scrambleLines(inputImage, perm);
        System.out.println(Brouillimg.breakKey2(scrambledImage, "Pearson"));
    }

    public static void testGetSKeyImage() throws IOException
    {
        BufferedImage inputImage = ImageIO.read(new File("out.png"));
        int[][] imageGL = Brouillimg.rgb2gl(inputImage);
        int[] ss = Brouillimg.getSsKeyImage(imageGL);
        System.out.println(ss[0]);
        System.out.println(ss[1]);
    }

    public static int myAlgoBreakKey()
    {
        return 0;
    }
}