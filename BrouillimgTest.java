public class BrouillimgTest
{

    public static void main(String[] args)
    {
        testGeneratePermutation();
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

}