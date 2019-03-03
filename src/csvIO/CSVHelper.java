package csvIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * https://raw.githubusercontent.com/agilepro/mendocino/master/src/com/purplehillsbooks/streams/CSVHelper.java
 * 
 * Helps to read and write a CSV file, all methods are static writeLine:
 * Converts list of String values into a line of a CSV file parseLine: read a
 * line from a LineNumberReader and return the list of Strings
 *
 * That should be all you need. Create or open the file and streams yourself from
 * whatever source you need to read from.. Everything in this class works on
 * characters, and not bytes.
 */
public class CSVHelper {

	/**
	 * Just a convenience method that iterates teh rows of a table and outputs
	 * to a writer which is presumably a CSV file.
	 * 
	 * @param w writer
	 * @param table data rows
	 * @throws Exception general exception
	 */
	public static void writeTable(Writer w, List<List<String>> table) throws Exception {
		for (int i=0; i<table.size(); i++) {
			List<String> row = table.get(i);
			writeLine(w, row);
		}
	}

	/**
	 * Write a single row of a CSV table, all values are quoted
	 * 
	 * @param w writer
	 * @param values row entries
	 * @throws Exception general exception general exception
	 */
	public static void writeLine(Writer w, List<String> values) throws Exception {
		boolean firstVal = true;
		for (String val : values) {
			if (!firstVal) {
				w.write(",");
			}
			w.write("\"");
			for (int i = 0; i < val.length(); i++) {
				char ch = val.charAt(i);
				if (ch == '\"') {
					w.write("\""); // extra quote
				}
				w.write(ch);
			}
			w.write("\"");
			firstVal = false;
		}
		w.write("\n");
	}

	/**
	 * @param r reader
	 * @return returns a row of values as a list. Returns null if you are past the end of the line
	 * @throws Exception general exception
	 */
	public static List<String> parseLine(Reader r) throws Exception {
		int ch = r.read();
		while (ch == '\r') {
			//ignore linefeed characters wherever they are, particularly just before end of file
			ch = r.read();
		}
		if (ch<0) {
			return null;
		}
		ArrayList<String> store = new ArrayList<String>();
		StringBuilder curVal = new StringBuilder();
		boolean inquotes = false;
		boolean started = false;
		while (ch>=0) {
			/* Don't read special characters. */
			if (ch <= '~')
			{
				if (inquotes) {
					started=true;
					if (ch == '\"') {
						inquotes = false;
					}
					else {
						curVal.append((char)ch);
					}
				}
				else {
					if (ch == '\"') {
						inquotes = true;
						if (started) {
							// if this is the second quote in a value, add a quote
							// this is for the double quote in the middle of a value
							curVal.append('\"');
						}
					}
					else if (ch == ',') {
						store.add(curVal.toString());
						curVal = new StringBuilder();
						started = false;
					}
					else if (ch == '\r') {
						//ignore LF characters
					}
					else if (ch == '\n') {
						//end of a line, break out
						break;
					}
					else {
						curVal.append((char)ch);
					}
				}
			}
			ch = r.read();
		}
		store.add(curVal.toString());
		return store;
	}

	/**
	 * 
	 * @param filename data file
	 * @return list of data rows
	 */
	public static List<List<String>> readFile(String filename)
	{
		BufferedReader br = null;
		FileReader fr = null;
		List<String> values = null;
		List<List<String>> out = new ArrayList<List<String>>();

		try { fr = new FileReader(filename); }
		catch (FileNotFoundException e1) { e1.printStackTrace(); }

		br = new BufferedReader(fr);

		try {
			values = CSVHelper.parseLine(br);
			while (values != null)
			{
				out.add(values); 
				values = CSVHelper.parseLine(br);
			}
		} catch (Exception e) { 	e.printStackTrace(); }
		return out;
	}

	/**
	 * 
	 * @param data data rows
	 * @param filename output file
	 */
	public static void writeFile(List<List<String>> data, String filename)
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			CSVHelper.writeTable(bw, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Convenience method to transpose the contents of a CSV file.
	 * @author michaelfrancenelson
	 * @param in input data rows
	 * @return transposed data
	 */
	public static List<List<String>> transpose(List<List<String>> in)
	{ return transpose(in, false); }
	
	public static List<List<String>> transpose(List<List<String>> in, boolean trim)
	{
		List<List<String>> out = new ArrayList<List<String>>();

		/* Check that all the rows are the same size.*/
		int length = in.get(0).size();
		
		/* if filling in for unequal rows, check for longest row: */
		if (trim)
		{
			for (List<String> l : in) 
			{
				int n = l.size();
				if (n > length) length = n;
			}
		}
		else 
		{
		for (List<String> l : in) if (l.size() != length) 
			throw new IllegalArgumentException("All rows are not the same length.");
		}
		
		int nNewRows = length;
		int nNewColumns = in.size();

		for (int i = 0; i < length; i++) out.add(new ArrayList<String>());

		for (int i = 0; i < nNewColumns; i++)
		{
			List<String> oldRow = in.get(i);
			if (oldRow.size() <= nNewRows)
//				if (oldRow.size() == nNewRows)
			for (int j = 0; j < oldRow.size(); j++)
//				for (int j = 0; j < nNewRows; j++)
			{
				out.get(j).add(oldRow.get(j));
			}
		}
		return out;
	}

//	public static void main(String[] args) {
//		String filename = "testOutput/annotatedTestBean.csv";
//		String filenameTr = "testOutput/annotatedTestBeanTransposed.csv";
//
//		List<List<String>> ll = readFile(filename);
//
//		List<TestBean> lb = AnnotatedBeanBuilder.factory(TestBean.class, ll, false);
//
//		AnnotatedBeanReporter<TestBean> rep =
//				AnnotatedBeanReporter.factory(TestBean.class, "%.4f", ",", "field1", "field2");
//		for (TestBean b : lb)	rep.consoleReport(b);
//
//		List<List<String>> lt = transpose(readFile(filename));
//
//		System.out.println("old row count: " + ll.size() + ", old column count: " + ll.get(0).size());
//		System.out.println("new row count: " + lt.size() + ", new column count: " + lt.get(0).size());
//
//		writeFile(lt, filenameTr);
//	}
}
