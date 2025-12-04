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
        testConvertPixelsToLines();
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

    public static void testImageString() throws IOException
    {
    	
    }

    public static void testConvertPixelsToLines()
    {
    	int[] trucs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
    	int[][][] amogus = Brouillimg.convertPixelsToLines(trucs, 3, 2);
    	for (int i = 0; i < 2; i++)
    	{
    		System.out.println(i);
    		for (int j = 0; j < 3; j++)
    		{
    			printArray(amogus[i][j]);
    		}
    	}
    }
}