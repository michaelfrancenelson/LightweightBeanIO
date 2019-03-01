package beans;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import csvIO.CSVHelper;
import xlsx.XLSXHelper;

public class AnnotatedBeanBuilder 
{
	/** Marker to show which of the bean's fields are to read or reported 
	 * 
	 * @author michaelfrancenelson
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface FieldColumn{};

	private static Random r = new Random();

	/**  
	 * @param c
	 * @return all the fields containing the @FieldColumn annotation
	 */
	public static <T> List<Field> getAnnotatedFields(Class<T> c)
	//	public static <T extends AnnotatedCSVBean> List<Field> getAnnotatedFields(Class<T> c)
	{
		List<Field> ll = new ArrayList<>();
		for (Field f : c.getDeclaredFields())
		{	f.setAccessible(true);
		if (f.isAnnotationPresent(FieldColumn.class)) ll.add(f);}
		return ll;
	}

	/** Parse an integer to a boolean value, in the style of R
	 * @param i if i is greater than zero returns true, false otherwise
	 * @return
	 */
	static boolean parseBool(int i) { if (i > 0) return true; return false; }

	/** Parse a string to a boolean value
	 * @param s 
	 * @return matches {"true", "t", "1"} to true and {"false", "f", "0"} to false.   
	 */
	private static boolean parseBool(String s)
	{
		String ss = s.trim();
		if (ss.equalsIgnoreCase("true")) return true;
		if (ss.equalsIgnoreCase("false")) return false;
		if (ss.equalsIgnoreCase("t")) return true;
		if (ss.equalsIgnoreCase("f")) return false;
		if (ss.equalsIgnoreCase("1")) return true;
		if (ss.equalsIgnoreCase("0")) return false;
		throw new IllegalArgumentException("Input: " + s + " could not be parsed to a boolean value");
	}

	public static <T> List<T> csvFactory(Class<T> clazz, String filename) { return csvFactory(clazz, filename, false); }
	
	public static <T> List<T> csvFactory(Class<T> clazz, String filename, boolean transposed)
	{

		List<List<String>> data = CSVHelper.readFile(filename);
		return factory(clazz, data, transposed);
	}

	public static <T> List<T> xlsxFactory(Class<T> clazz, String filename) { return xlsxFactory(clazz, filename, false); }
	public static <T> List<T> xlsxFactory(Class<T> clazz, String filename, boolean transposed)
	{
		List<List<String>> data = XLSXHelper.readXLSX(filename);
		return factory(clazz, data, transposed);
	}

	/** Create annotated bean instances
	 * @param clazz 
	 * @param data data for the beans, the first row must contain the headers.
	 * @return
	 */
	public static <T> List<T> factory(Class<T> clazz, List<List<String>> data, boolean transposed)
	{
		if (transposed) data = CSVHelper.transpose(data);
		List<Field> ff = getAnnotatedFields(clazz);
		List<String> headers = data.get(0);

		/* Headers in the data that don't match a field in clazz are ignored. */
		//		if (ff.size() != headers.size()) 
		//			throw new IllegalArgumentException("The input data headers do not "
		//					+ "match the field names for type " + clazz.getSimpleName());

		List<String> row;
		List<T> out = new ArrayList<>();
		T o = null;
		try 
		{
			for (int r = 1; r < data.size(); r++)
			{
				row = data.get(r);
				o = clazz.newInstance();
				for (int i = 0; i < ff.size(); i++) 
				{
					Field f = ff.get(i);
					String name = f.getName();
					int whichColumn = headers.indexOf(name);

					if (whichColumn >= 0)
					{
						String val = row.get(whichColumn);
						setVal(f, val, o);
					}
				}
				out.add(o);
			}
		}
		catch (InstantiationException | IllegalAccessException e) 
		{e.printStackTrace();}

		return out;
	}

	/** Build a bean instance with randomized values for the annotated fields. */
	public static <T> List<T> randomFactory(Class<T> clazz, int n)
	{
		List<T> l = new ArrayList<>();
		for (int i = 0; i < n; i++) l.add(randomFactory(clazz));
		return l;
	}

	/** Build a list of bean instances with randomized values for the annotated fields. */
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

	/** Set the value of the field to the (appropriately casted) value. */
	private static <T> void setVal(Field f, String val, T o)
	{
		if (f.isAnnotationPresent(FieldColumn.class))
		{
			String shortName = f.getType().getSimpleName();
			try {
				switch (shortName) {
				case("int"):     
					f.setInt(o, Integer.parseInt(val));
				break;
				case("double"):  f.setDouble(o,  Double.parseDouble(val)); break;
				case("boolean"): f.setBoolean(o, parseBool(val)); break;
				case("String"):	 f.set(o, val); break;
				case("Integer"): f.set(o, (Integer) Integer.parseInt(val)); break;
				case("Double"):  f.set(o, (Double) Double.parseDouble(val)); break;
				case("Boolean"): f.set(o, (Boolean) parseBool(val)); break;
				case("char"):    f.setChar(o, val.charAt(0)); break;
				default: throw new IllegalArgumentException("Input value for field of type " 
						+ shortName + " could not be parsed");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {	e.printStackTrace();}
		}
	}

	/** Random int convenience generator.  Uses Java Random - not reseedable. */
	public static int     randInt(int min, int max) {return r.nextInt(max - min) + min; }
	/** Random double convenience generator.  Uses Java Random - not reseedable. */
	public static double  randDouble(double min, double max) { return (max - min) * r.nextDouble() + min; }
	/** Random char convenience generator.  Uses Java Random - not reseedable. */
	public static char    randChar(char min, char max) { return (char)(randInt(min, max)); }
	/** Random bool convenience generator.  Uses Java Random - not reseedable. */
	public static boolean randBool(double prob) { if (r.nextDouble() < prob) return true; return false; }



	public static <T> boolean equals(Class<T> clazz, T t1, T t2)
	{
		AnnotatedBeanReporter<T> rep = AnnotatedBeanReporter.factory(clazz, "%.4f", ",");
		List<String> r1 = rep.stringValReport(t1);
		List<String> r2 =rep.stringValReport(t2);
		if (r1.size() != r2.size()) return false;
		for (int i = 0; i < r1.size(); i++) 
			if (!(r1.get(i).equals(r2.get(i)))) 
			{
				System.out.println(r1.get(i) + " != " + r2.get(i));
				return false; 
			}
		return true;
	}

	/** Random generator for primitive or boxed primitive types. 
	 * 
	 * @param shortName
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
	 * @param nChars
	 * @param min
	 * @param max
	 * @return
	 */
	public static String randomString(int nChars, char min, char max) { return randomString(nChars, min, max, null);}
	/** Random String generator
	 *  
	 * @param nChars
	 * @param min
	 * @param max
	 * @param r
	 * @return
	 */
	public static String randomString(int nChars, char min, char max, Random r)
	{
		if (r == null) r = new Random();
		String s = "";
		for (int i = 0; i < nChars; i++) s += (char)(r.nextInt(max - min) + min);
		return s;
	}
}