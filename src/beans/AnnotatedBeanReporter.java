package beans;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import beans.AnnotatedBeanBuilder.FieldColumn;
import beans.GetterGetterGetter.StringValGetter;

/** Utilities for recording the states of annotated beans 
 *  and writing the results to a file
 * 
 * @author michaelfrancenelson
 *
 * @param <T> annotated bean type to report
 */
public class AnnotatedBeanReporter<T>
{
	private String dblFmt, sep;
	private Class<T> clazz;
	private List<StringValGetter<T>> getters;
	private List<String> headers;
	private String[] additionalColumnNames;
	private ByteArrayOutputStream bStreamOut;


	public static <T> String getStringVal(Class<T> clazz, T t, Field f,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar) throws IllegalArgumentException, IllegalAccessException
	{
		f.setAccessible(true);

		String type = f.getType().getSimpleName();

		switch(type)
		{
		case("int"):     { return String.format("%d", f.getInt(t)); }
		case("double"):  { return String.format("%f", f.getDouble(t)); }
		case("boolean"): { return Boolean.toString(f.getBoolean(t)); }

		case("String"):  { return f.get(t).toString(); }
		case("char"):    { return String.valueOf(f.getChar(t)); }

		case("Integer"): { return String.format("%d", (Integer) f.get(t)); }
		case("Double"):  { return String.format("%f", (Double) f.get(t)); }
		case("Boolean"): { return Boolean.toString((Boolean) f.get(t)); }
		default:         { return f.get(t).toString(); }
		}
	}

	static <T> boolean isNA(Class<T> clazz, T t, Field f,
			int naInt, double naDouble, String naString, char naChar) 
					throws IllegalArgumentException, IllegalAccessException
	{
		String type = f.getType().getSimpleName();

		switch(type)
		{
		case("int"):     { return  f.getInt(t) == naInt; }
		case("double"):  { return f.getDouble(t) == naDouble; }
		/* It doesn't really make sense to check this for the primitive boolean type... */
		//		case("boolean"): { return f.getBoolean(t) == naBoolean;; }

		case("String"):  { return f.get(t).toString().equals(naString); }
		case("char"):    { return f.getChar(t) == naChar; }

		case("Integer"): { return (Integer) f.get(t) == naInt; }
		case("Double"):  { return (Double) f.get(t) == naDouble; }
		case("Boolean"): { return (Boolean) f.get(t) == null; }
		default:         { return false; }
		}
	}

	/**
	 * 
	 * @param t bean object
	 * @return a list of string representations of the bean's annotated fields
	 */
	public List<String> stringValReport(T t)
	{
		List<String> l = new ArrayList<>(getters.size());
		for (StringValGetter<T> g : getters) 
			l.add(g.get(t));
		return l;
	}

	/**
	 * 
	 * @param clazz type of bean
	 * @param dblFmt how to print double values
	 * @param <T> annotated bean type to report
	 * @return a list of string representations of the bean's annotated fields
	 */
	public static <T> List<String> staticStringValReport(Class<T> clazz, String dblFmt)
	{
		List<StringValGetter<T>> getters;
		List<Field> fields = AnnotatedBeanBuilder.getAnnotatedFields(clazz, FieldColumn.class);

		List<Field> staticFields = new ArrayList<>();
		for (Field f : fields)
		{
			if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) staticFields.add(f);  
		}

		getters = GetterGetterGetter.stringValGetterGetter(clazz, fields, dblFmt);

		List<String> l = new ArrayList<>(getters.size());

		T t = null;
		try {
			t = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		for (StringValGetter<T> g : getters) 
			l.add(g.get(t));

		return l;
	}

	/** Print the bean's annotated fields to the console
	 * 
	 * @param t bean object
	 */
	public void consoleReport(T t)
	{
		List<String> vals = stringValReport(t);
		System.out.println("Bean of type: " + clazz.getSimpleName());
		for (int i = 0; i < vals.size(); i++)
		{
			System.out.println("field " + headers.get(i) + ": " + vals.get(i));
		}
		System.out.println();
	}

	/**
	 * @return a formatted string with the names of the bean's annotated fields
	 */
	public String headerReportLine() { return concat(headers, sep, (Object[]) additionalColumnNames); }
	/** 
	 * 
	 * @param t bean object
	 * @param additionalColumns additional items for the report
	 * @return a formatted string with string representations of the bean's annotated fields
	 */
	public String stringReportLine(T t, Object... additionalColumns)
	{ return concat(stringValReport(t), sep, additionalColumns); }

	/** Add a bean to the report. 
	 * 
	 * @param item bean to add 
	 * @param additionalColumns additional items for the report
	 */
	public void appendToReport(T item, Object... additionalColumns)
	{
		try {
			bStreamOut.write(System.lineSeparator().getBytes());
			bStreamOut.write(stringReportLine(item, additionalColumns).getBytes());
		}
		catch (IOException e) { e.printStackTrace(); }	
	}

	/** Add the current values of the annotated static variables to the report.
	 * 
	 * @param clazz class of bean
	 * @param additionalColumns additional items for the report
	 */
	public void appendStaticToReport(Class<T> clazz, Object... additionalColumns)
	{
		List<String> ll = staticStringValReport(clazz, dblFmt);
		concat(ll, sep, additionalColumns);
		try {
			bStreamOut.write(System.lineSeparator().getBytes());
			bStreamOut.write(concat(ll, sep, additionalColumns).getBytes());
		}
		catch (IOException e) { e.printStackTrace(); }	
	}

	/** Add a list of beans to the report. 
	 * 
	 * @param list bean list
	 * @param extraColumns extra items for the report
	 */
	public void appendListToReport(List<T> list, Object... extraColumns)
	{ for (T t : list) appendToReport(t, extraColumns); }

	/** Format input strings for appending to the report
	 * 
	 * @param l input strings
	 * @param sep separator (usually a comma)
	 * @param additionalCols additional items for the report
	 * @return concatenated string suitable for the report
	 */
	public static String concat(List<String> l, String sep, Object... additionalCols)
	{
		int nHeaders = l.size();
		int nExtra = additionalCols.length;
		int nElements = nHeaders + nExtra;
		String out = "";
		int i = 0;
		if (nElements > 0)
		{
			if (nHeaders > 0) {out += l.get(0); i++; }
			if (nHeaders > 1) while(i < nHeaders) { out += sep + l.get(i); i++; }
			if (nExtra > 0)	for (Object s : additionalCols)	out += sep + s;
		}
		return out;
	}

	/** Write the data to file and close the reporter.
	 * 
	 * @param filename input file
	 */
	public void writeCSV(String filename)
	{
		try {
			OutputStream out = new FileOutputStream(filename);
			bStreamOut.writeTo(out);
			bStreamOut.close();
			out.close();
		} 
		catch (IOException e) { e.printStackTrace();}
	}

	/** Build a reporter for the annotated bean type T
	 * 
	 * @param clazz type of bean
	 * @param dblFmt how to print double values
	 * @param sep usually a comma
	 * @param additionalColumns extra items for the report
	 * @param <T> annotated bean type to report
	 * @return reporter for type clazz
	 */
	public static <T> AnnotatedBeanReporter<T> 
	factory(Class<T> clazz, String dblFmt, String sep, String... additionalColumns)
	{
		AnnotatedBeanReporter<T> rep = new AnnotatedBeanReporter<>();
		rep.clazz = clazz;
		List<Field> fields = AnnotatedBeanBuilder.getAnnotatedFields(rep.clazz, FieldColumn.class);
		rep.dblFmt = dblFmt;
		rep.sep = sep;
		rep.getters = GetterGetterGetter.stringValGetterGetter(
				rep.clazz, fields, rep.dblFmt);
		rep.headers = GetterGetterGetter.columnHeaderGetter(fields);
		rep.additionalColumnNames = additionalColumns;
		rep.bStreamOut = new ByteArrayOutputStream();
		try	{ rep.bStreamOut.write(rep.headerReportLine().getBytes()); } 
		catch (IOException e) { e.printStackTrace(); }
		return rep;
	}
}