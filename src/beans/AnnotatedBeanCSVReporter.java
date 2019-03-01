package beans;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import beans.GetterGetterGetter.StringValGetter;

/** Utilities for recording the states of annotated beans 
 *  and writing the results to a file
 * 
 * @author michaelfrancenelson
 *
 * @param <T>
 */
public class AnnotatedBeanCSVReporter<T extends AnnotatedCSVBean>
{
	private String dblFmt, sep;
	private Class<T> clazz;
	private List<StringValGetter<T>> getters;
	private List<String> headers;
	private String[] additionalColumnNames;
	private ByteArrayOutputStream bStreamOut;
	
	/**
	 * 
	 * @param t
	 * @return a list of string representations of the bean's annotated fields
	 */
	public List<String> stringValReport(T t)
	{
		List<String> l = new ArrayList<>(getters.size());
		for (StringValGetter<T> g : getters) l.add(g.get(t));
		return l;
	}

	/** Print the bean's annotated fields to the console
	 * 
	 * @param t
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
	 * 
	 * @return a formatted string with the names of the bean's annotated fields
	 */
	public String headerReportLine() { return concat(headers, sep, (Object[]) additionalColumnNames); }
	/** 
	 * 
	 * @param t
	 * @param additionalColumns
	 * @return a formatted string with string representations of the bean's annotated fields
	 */
	public String stringReportLine(T t, Object... additionalColumns)
	{ return concat(stringValReport(t), sep, additionalColumns); }

	/** Add a bean to the report. */
	public void appendToReport(T item, Object... additionalColumns)
	{
		try {
			bStreamOut.write(System.lineSeparator().getBytes());
			bStreamOut.write(stringReportLine(item, additionalColumns).getBytes());
		}
		catch (IOException e) { e.printStackTrace(); }	
	}

	/** Add a list of beans to the report. */
	public void appendListToReport(List<T> list, Object... extraColumns)
	{ for (T t : list) appendToReport(t, extraColumns); }
	
	/** Format input strings for appending to the report
	 * 
	 * @param l
	 * @param sep
	 * @param additionalCols
	 * @return
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
	
	/** Write the data to file and close the reporter. */
	public void write(String filename)
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
	 * @param clazz
	 * @param dblFmt
	 * @param sep
	 * @param additionalColumns
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> AnnotatedBeanCSVReporter<T> 
	factory(Class<T> clazz, String dblFmt, String sep, String... additionalColumns)
	{
		AnnotatedBeanCSVReporter<T> rep = new AnnotatedBeanCSVReporter<>();
		rep.clazz = clazz;
		List<Field> fields = AnnotatedCSVBean.getAnnotatedFields(rep.clazz);
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