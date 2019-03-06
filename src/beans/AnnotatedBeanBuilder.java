package beans;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import csvIO.CSVHelper;
import xlsx.XLSXHelper;

public class AnnotatedBeanBuilder {
	//	static Logger logger = LoggerFactory.getLogger(AnnotatedBeanBuilder.class);

	/**
	 * Marker to show which of the bean's fields are to read or reported
	 * 
	 * @author michaelfrancenelson
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface FieldColumn {
	};

	/**
	 * Marker for fields that can be checked for initialized status
	 * 
	 * @author michaelfrancenelson
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Initialized {
	};

	/**
	 * Build list of beans from an input csv or xlsx file with individual bean data
	 * arranged in rows. Incomplete rows/columns will result in an error
	 * 
	 * @param clazz    class of bean
	 * @param filename name of input file
	 * @param          <T> bean type
	 * @return list of bean objects
	 */
	public static <T> List<T> factory(Class<T> clazz, String filename) {
		return factory(clazz, filename, false);
	}

	/**
	 * Build list of beans from an input csv or xlsx file with individual bean data
	 * arranged in rows;
	 * 
	 * @param clazz    class of bean
	 * @param filename name of input file
	 * @param trim     should incomplete data rows be ignored?
	 * @param          <T> bean type
	 * @return list of bean objects
	 */
	public static <T> List<T> factory(Class<T> clazz, String filename, boolean trim) {
		List<List<String>> data = getData(filename);
		boolean rows = testRowOrientation(clazz, data);
		return factory(clazz, data, !rows, trim);
	}

	/**
	 * @param clazz bean class
	 * @param       <T> bean type
	 * @return all the fields containing the @FieldColumn annotation
	 */
	public static <T> List<Field> getAnnotatedFields(Class<T> clazz) {
		List<Field> ll = new ArrayList<>();
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			if (f.isAnnotationPresent(FieldColumn.class))
				ll.add(f);
		}
		return ll;
	}

	/**
	 * 
	 * @param clazz    bean class
	 * @param annClazz annotation class
	 * @param          <T> bean type
	 * @return
	 */
	public static <T, A extends Annotation> List<Field> getAnnotatedFields(Class<T> clazz, Class<A> annClazz) {
		List<Field> ll = new ArrayList<>();
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			if (f.isAnnotationPresent(annClazz))
				ll.add(f);
		}
		return ll;
	}

	/**
	 * 
	 * @param filename data file
	 * @return data rows
	 */
	private static List<List<String>> getData(String filename) {
		if (filename.endsWith("xlsx"))
			return XLSXHelper.readXLSX(filename);
		else if (filename.endsWith(".csv"))
			return CSVHelper.readFile(filename);
		throw new IllegalArgumentException(
				"Input file " + filename + " does not appear " + "to be in either the XLSX or CSV format.");
	}

	/**
	 * Create annotated bean instances
	 * 
	 * @param clazz class of bean
	 * @param       <T> bean type
	 * @param data  data for the beans, the first row must contain the headers.
	 * @return list of bean objects
	 */
	public static <T> List<T> factory(Class<T> clazz, List<List<String>> data, boolean transposed) {
		return factory(clazz, data, transposed, false);
	}

	/**
	 * Create annotated bean instances
	 * 
	 * @param clazz class of bean
	 * @param       <T> bean type
	 * @param data  data for the beans, the first row must contain the headers.
	 * @param trim  should incomplete rows be removed?
	 * @return list of bean objects
	 */
	public static <T> List<T> factory(Class<T> clazz, List<List<String>> data,
			boolean transposed, boolean trim) {
		if (transposed)
			data = CSVHelper.transpose(data, trim);
		List<Field> ff = getAnnotatedFields(clazz);
		List<String> headers = data.get(0);

		List<String> row;
		List<T> out = new ArrayList<>();
		T o = null;
		try {
			for (int r = 1; r < data.size(); r++) {
				try {

					row = data.get(r);

					o = clazz.newInstance();
					for (int i = 0; i < ff.size(); i++) {
						Field f = ff.get(i);
						String name = f.getName();
						int whichColumn = headers.indexOf(name);

						if (whichColumn >= 0) {
							String val = row.get(whichColumn);
							setVal(f, val, o);
						}
					}
				} catch (NumberFormatException e) {
				} catch (IndexOutOfBoundsException e) { // In case of an incomplete row of data
				} catch (IllegalArgumentException e) {
				}
				out.add(o);
			}
		} catch (InstantiationException | IllegalAccessException e) {
		}
		return out;
	}

	/**
	 * Parse an integer to a boolean value, in the style of R
	 * 
	 * @param i if i is greater than zero returns true, false otherwise
	 * @return i if i is greater than zero returns true, false otherwise
	 */
	static boolean parseBool(int i) {
		if (i > 0)
			return true;
		return false;
	}

	/**
	 * Parse a string to a boolean value
	 * 
	 * @param s string to parse
	 * @return matches {"true", "t", "1"} to true and {"false", "f", "0"} to false.
	 */
	private static boolean parseBool(String s) {
		String ss = s.trim();
		if (ss.equalsIgnoreCase("true"))
			return true;
		if (ss.equalsIgnoreCase("false"))
			return false;
		if (ss.equalsIgnoreCase("t"))
			return true;
		if (ss.equalsIgnoreCase("f"))
			return false;
		if (ss.equalsIgnoreCase("1"))
			return true;
		if (ss.equalsIgnoreCase("0"))
			return false;
		throw new IllegalArgumentException("Input: " + s + " could not be parsed to a boolean value");
	}

	/**
	 * Simple check that input data contains entries for all the annotated fields.
	 * 
	 * @param clazz class of bean
	 * @param data  input data rows
	 * @param       <T> bean type
	 * @return true = data oriented in rows, false = data oriented in columns
	 */
	private static <T> boolean testRowOrientation(Class<T> clazz, List<List<String>> data) {
		List<Field> ff = getAnnotatedFields(clazz);
		List<String> fieldNames = new ArrayList<>();
		for (Field f : ff)
			fieldNames.add(f.getName());

		boolean test = true;


		List<String> absentFromRow = new ArrayList<>();
		List<String> absentFromCol = new ArrayList<>();

		List<String> presentInRow = new ArrayList<>();
		List<String> presentInCol = new ArrayList<>();

		/* Test row orientation */
		List<String> row = data.get(0);
		for (String f : fieldNames) {
			if (!row.contains(f)) {
				test = false;
				absentFromRow.add(f);
			}
			else presentInRow.add(f);
		}

		if (absentFromRow.size() == 0) return true;

		/* Test for column orientation. */
		test = true;
		List<String> firstCol = new ArrayList<>();
		for (List<String> l : data) {
			firstCol.add(l.get(0));
		}



		for (String f : fieldNames) {
			if (!firstCol.contains(f)) {
				test = false;
				absentFromCol.add(f);
			}
			else presentInCol.add(f);
		}

		if (absentFromCol.size() == 0) return false;


		String message = "Problem parsing input file. ";
		if (presentInRow.size() > 1)
		{
			message += "File appears to have data oriented in rows. " +
					"Check for column headings: \n" +
					AnnotatedBeanReporter.concat(absentFromRow, " ,");
		}
		else if (presentInRow.size() > 1)
		{
			message += "File appears to have data oriented in columns. " +
					"Check for row headings: \n" +
					AnnotatedBeanReporter.concat(absentFromCol, " ,");
		}
		else message += " Could not determine orientation of data in input file";

		throw new IllegalArgumentException(message);
	}

	/**
	 * Set the value of the field to the (appropriately casted) value.
	 * 
	 * @param f   annotated field
	 * @param val value to set
	 * @param o   bean
	 * @param     <T> bean type
	 */
	protected static <T> void setVal(Field f, String val, T o) {
		if (f.isAnnotationPresent(FieldColumn.class)) {
			String shortName = f.getType().getSimpleName();
			try {
				switch (shortName) {
				case ("int"):
					f.setInt(o, Integer.parseInt(val));
				break;
				case ("double"):
					f.setDouble(o, Double.parseDouble(val));
				break;
				case ("boolean"):
					f.setBoolean(o, parseBool(val));
				break;
				case ("String"):
					f.set(o, val);
				break;
				case ("Integer"):
					f.set(o, (Integer) Integer.parseInt(val));
				break;
				case ("Double"):
					f.set(o, (Double) Double.parseDouble(val));
				break;
				case ("Boolean"):
					f.set(o, (Boolean) parseBool(val));
				break;
				case ("char"):
					f.setChar(o, val.charAt(0));
				break;
				default:
					throw new IllegalArgumentException(
							"Input value for field of type " + shortName + " could not be parsed");

				}
				//				logger.trace("Field " + f.getName() + "(" + shortName + ")" + " set to " + val + ".");
			} catch (NumberFormatException e) {
				throw e;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test that all the annotated fields of two beans are the same.
	 * 
	 * @param clazz class of beans
	 * @param t1    bean 1
	 * @param t2    bean 1
	 * @param       <T> type of bean
	 * @return are they equal?
	 */
	public static <T> boolean equals(Class<T> clazz, T t1, T t2) {
		AnnotatedBeanReporter<T> rep = AnnotatedBeanReporter.factory(clazz, "%.4f", ",");
		List<String> r1 = rep.stringValReport(t1);
		List<String> r2 = rep.stringValReport(t2);
		if (r1.size() != r2.size())
			return false;
		for (int i = 0; i < r1.size(); i++)
			if (!(r1.get(i).equals(r2.get(i)))) {
				//				logger.debug(r1.get(i) + " != " + r2.get(i));
				return false;
			}
		return true;
	}
}