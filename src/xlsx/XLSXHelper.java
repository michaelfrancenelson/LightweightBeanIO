package xlsx;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/** https://stackoverflow.com/questions/52261887/light-weight-xlsx-reader-in-java
 * 
 * @author michaelfrancenelson, modified from user Vivek Aditya's answer on the SO page
 *
 */
public class XLSXHelper {

//	static Logger logger = Logger.getLogger(XLSXHelper.class.getName());
	
	public static List<List<String>> readXLSX(String filename)
	{
//		logger.setLevel(Level.WARNING);
		
		List<List<String>> result = new ArrayList<>();
		XMLEventReader reader = null;
		XMLEvent event = null;
		Attribute attribute = null;
		StartElement startElement = null;
		EndElement endElement = null;
		String characters = null;

		StringBuilder stringValue = new StringBuilder(); //for collecting the characters to complete values
		List<String> sharedStrings = new ArrayList<String>(); //list of shared strings
		Map<String, String> numberFormats = new HashMap<String, String>(); //map of number formats
		List<String> cellNumberFormats = new ArrayList<String>(); //list of cell number formats
		Path source = Paths.get(filename); //path to the Excel file

		//get filesystem of Excel file
		FileSystem fs = null;
		/* Giant try block - not the best practice... */
		try {
			fs = FileSystems.newFileSystem(source, null); 

			//get shared strings ==============================================================================
			Path sharedStringsTable = fs.getPath("/xl/sharedStrings.xml");

			reader = XMLInputFactory.newInstance().createXMLEventReader(Files.newInputStream(sharedStringsTable));
			boolean siFound = false;
			while (reader.hasNext()) {
				event = (XMLEvent) reader.next();
				if (event.isStartElement()) {
					startElement = (StartElement) event;
					if (startElement.getName().getLocalPart().equalsIgnoreCase("si")) {
						//start element of shared string item
						siFound = true;
						stringValue = new StringBuilder();
					}
				} else if (event.isCharacters() && siFound) {
					//chars of the shared string item
					characters = event.asCharacters().getData();
					stringValue.append(characters);
				} else if (event.isEndElement()) {
					endElement = (EndElement) event;
					if (endElement.getName().getLocalPart().equalsIgnoreCase("si")) {
						//end element of shared string item
						siFound = false;
						sharedStrings.add(stringValue.toString());
					}
				}
			}

			reader.close();
			//shared strings ==================================================================================

			//get styles, number formats are essential for detecting date / time values =======================
			Path styles = fs.getPath("/xl/styles.xml");
			reader = XMLInputFactory.newInstance().createXMLEventReader(Files.newInputStream(styles));
			boolean cellXfsFound = false;
			while (reader.hasNext()) {
				event = (XMLEvent) reader.next();
				if (event.isStartElement()) {
					startElement = (StartElement) event;
					if (startElement.getName().getLocalPart().equalsIgnoreCase("numFmt")) {
						//start element of number format
						attribute = startElement.getAttributeByName(new QName("numFmtId"));
						String numFmtId = attribute.getValue();
						attribute = startElement.getAttributeByName(new QName("formatCode"));
						numberFormats.put(numFmtId, ((attribute != null) ? attribute.getValue() : "null"));
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("cellXfs")) {
						//start element of cell format setting
						cellXfsFound = true;
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("xf") && cellXfsFound) {
						//start element of format setting in cell format setting
						attribute = startElement.getAttributeByName(new QName("numFmtId"));
						cellNumberFormats.add(((attribute != null) ? attribute.getValue() : "null"));
					}
				} else if (event.isEndElement()) {
					endElement = (EndElement) event;
					if (endElement.getName().getLocalPart().equalsIgnoreCase("cellXfs")) {
						//end element of cell format setting
						cellXfsFound = false;
					}
				}
			}
			reader.close();
			//styles ==========================================================================================

			//get sheet data of first sheet ===================================================================
			Path sheet1 = fs.getPath("/xl/worksheets/sheet1.xml");
			reader = XMLInputFactory.newInstance().createXMLEventReader(Files.newInputStream(sheet1));
			boolean rowFound = false;
			boolean cellFound = false;
			boolean cellValueFound = false;
			boolean inlineStringFound = false;
			//        String cellStyle = null;
			String cellType = null;
			List<String> row = null;
			
//			int i = 1, j = 1;
			while (reader.hasNext()) {
				event = (XMLEvent) reader.next();
				if (event.isStartElement()) {
					startElement = (StartElement) event;
					if (startElement.getName().getLocalPart().equalsIgnoreCase("row")) {
						//start element of row
						rowFound = true;
						if (row != null) {
							result.add(row); 
//							logger.info("XLSXHelper: adding row " + i + " to results."); //TODO
						}
						row = new ArrayList<>();
//						logger.info("XLSXHelper: creating row " + i); i++; //TODO 
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("c") && rowFound) {
						//start element of cell in row
						cellFound = true;
						attribute = startElement.getAttributeByName(new QName("t"));
						cellType = ((attribute != null) ? attribute.getValue() : null);
						attribute = startElement.getAttributeByName(new QName("s"));
						//                    cellStyle = ((attribute != null) ? attribute.getValue() : null);
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("v") && cellFound) {
						//start element of value in cell
						cellValueFound = true;
						stringValue = new StringBuilder();
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("is") && cellFound) {
						//start element of inline string in cell
						inlineStringFound = true;
						stringValue = new StringBuilder();
					}
				} else if (event.isCharacters() && cellFound && (cellValueFound || inlineStringFound)) {
					//chars of the cell value or the inline string
					characters = event.asCharacters().getData();
					stringValue.append(characters);
				} else if (event.isEndElement()) {
					endElement = (EndElement) event;
					if (endElement.getName().getLocalPart().equalsIgnoreCase("row")) {
						//end element of row
						rowFound = false;
					} else if (endElement.getName().getLocalPart().equalsIgnoreCase("c")) {
						//end element of cell
						cellFound = false;
					} else if (endElement.getName().getLocalPart().equalsIgnoreCase("v")) {
						//end element of value
						cellValueFound = false;
						String cellValue = stringValue.toString();
						if ("s".equals(cellType)) {
							cellValue = sharedStrings.get(Integer.valueOf(cellValue));
						}
						row.add(cellValue);
//						logger.info("XLSXHelper: adding " + cellValue + " to row " + i); //TODO 

					} else if (endElement.getName().getLocalPart().equalsIgnoreCase("is")) {
						//end element of inline string
						inlineStringFound = false;
						String cellValue = stringValue.toString();
						row.add(cellValue);
//						logger.info("XLSXHelper: adding " + cellValue + " to row " + i); //TODO 
					}
				}
			
			}
			if (row != null) {
				result.add(row); 
//				logger.info("XLSXHelper: adding row " + i + " to results."); //TODO
			}
			reader.close();
			//sheet data ======================================================================================
			fs.close();
		}
		catch (XMLStreamException | FactoryConfigurationError | IOException e)
		{throw new IllegalArgumentException("Could not process " + filename);} 
		return result;
	}
}
