package beans;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnnotatedCSVBean 
{
	/** Marker to show which of the bean's fields are to read or reported 
	 * 
	 * @author michaelfrancenelson
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface FieldColumn{};

	/**  
	 * @param c
	 * @return all the fields containing the @FieldColumn annotation
	 */
	public static <T extends AnnotatedCSVBean> List<Field> getAnnotatedFields(Class<T> c)
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

	/** Create annotated bean instances
	 * @param clazz 
	 * @param data data for the beans, the first row must contain the headers.
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> List<T> factory(Class<T> clazz, 
			List<List<String>> data)
	{
		List<Field> ff = getAnnotatedFields(clazz);
		List<String> headers = data.get(0);

		if (ff.size() != headers.size()) 
			throw new IllegalArgumentException("The input data headers do not "
					+ "match the field names for type " + clazz.getSimpleName());
		
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
					String val = row.get(whichColumn);

					if (f.isAnnotationPresent(FieldColumn.class))
					{
						String shortName = f.getType().getSimpleName();
						switch (shortName) {
						case("int"):     f.setInt(o, Integer.parseInt(val)); break;
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
					}
				}
				out.add(o);
			}
		}
		catch (InstantiationException | IllegalAccessException e) 
		{e.printStackTrace();}

		return out;
	}

	public static String randomString(int nChars, char min, char max) { return randomString(nChars, min, max, null);}
	public static String randomString(int nChars, char min, char max, Random r)
	{
		if (r == null) r = new Random();
		String s = "";
		for (int i = 0; i < nChars; i++) s += (char)(r.nextInt(max - min) + min);
		return s;
	}
}
