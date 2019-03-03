package beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBeanBuilder extends AnnotatedBeanBuilder 
{
	private static Random r = new Random();

	/** Build a bean instance with randomized values for the annotated fields. 
	 * 
	 * @param clazz bean class
	 * @param n number of beans to make
	 * @param <T> bean type
	 * @return list of beans
	 */
	public static <T> List<T> randomFactory(Class<T> clazz, int n)
	{
		List<T> l = new ArrayList<>();
		for (int i = 0; i < n; i++) l.add(randomFactory(clazz));
		return l;
	}

	/** Build a list of bean instances with randomized values for the annotated fields. 
	 * 
	 * @param clazz bean class
	 * @param <T> bean type
	 * @return list of beans
	 */
	public static <T> T randomFactory(Class<T> clazz)
	{
		List<Field> ff = getAnnotatedFields(clazz);
		T o = null;
		try 
		{
			o = clazz.newInstance();

			for (int i = 0; i < ff.size(); i++) 
			{
				Field f = ff.get(i);
				String shortName = f.getType().getSimpleName();
				String val = randomString(shortName);
				setVal(f, val, o);
			}
		}
		catch (InstantiationException | IllegalAccessException e) 
		{e.printStackTrace();}
		return o;
	}

	/** Random int convenience generator.  Uses Java Random - not reseedable. 
	 * 
	 * @param min min value
	 * @param max max value
	 * @return random value
	 */
	public static int     randInt(int min, int max) {return r.nextInt(max - min) + min; }
	/** Random double convenience generator.  Uses Java Random - not reseedable. 
	 * 
	 * @param min min value
	 * @param max max value
	 * @return random double
	 */
	public static double  randDouble(double min, double max) { return (max - min) * r.nextDouble() + min; }
	/** Random char convenience generator.  Uses Java Random - not reseedable. 
	 * 
	 * @param min min char
	 * @param max highest char
	 * @return random char
	 */
	public static char    randChar(char min, char max) { return (char)(randInt(min, max)); }
	/** Random bool convenience generator.  Uses Java Random - not reseedable. 
	 * 
	 * @param prob probability of true
	 * @return true/false
	 */
	public static boolean randBool(double prob) { if (r.nextDouble() < prob) return true; return false; }


	/** Random generator for primitive or boxed primitive types. 
	 * 
	 * @param shortName short name of type
	 * @return String representation of the random data. 
	 */
	public static String randomString(String shortName)
	{
		String val = "";
		switch (shortName) {
		case("int"):     val = String.format("%d", randInt(0, 100)); break;
		case("double"):  val = String.format("%f", randDouble(0, 100)); break;
		case("boolean"): val = Boolean.toString(randBool(0.5)); break;
		case("String"):	 val = randomString(r.nextInt(12) + 1, '9', 'Z'); break; 

		case("Integer"): val = String.format("%d", randInt(0, 100)); break;
		case("Double"):  val = String.format("%f", randDouble(0, 100)); break;
		case("Boolean"): val = Boolean.toString(randBool(0.5)); break;

		case("char"):    val = String.format("%s", randChar('9', 'Z')); break;

		default: throw new IllegalArgumentException("Input value for field of type " 
				+ shortName + " could not be parsed");
		}	
		return val;
	}

	/** Random String generator 
	 * 
	 * @param nChars how long shoudl the string be?
	 * @param min what is the highest character?
	 * @param max what is the lowest character
	 * @return random string
	 */
	public static String randomString(int nChars, char min, char max) { return randomString(nChars, min, max, null);}
	/** Random String generator
	 *  
	 * @param nChars how long shoudl the string be?
	 * @param min what is the highest character?
	 * @param max what is the lowest character
	 * @param r random generator
	 * @return random string
	 */
	public static String randomString(int nChars, char min, char max, Random r)
	{
		if (r == null) r = new Random();
		String s = "";
		for (int i = 0; i < nChars; i++) s += (char)(r.nextInt(max - min) + min);
		return s;
	}

}
