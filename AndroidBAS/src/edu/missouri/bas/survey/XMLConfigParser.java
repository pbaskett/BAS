package edu.missouri.bas.survey;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class XMLConfigParser {
	private XMLReader initializeReader() throws SAXException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		return reader;
	}
	
	public ArrayList<SurveyInfo> parseQuestion(InputSource XML){
		try{
			XMLReader reader = initializeReader();
			
			XMLConfigHandler xmlHandler = new XMLConfigHandler();
			
			reader.setContentHandler(xmlHandler);
			reader.parse(XML);		
			
			return xmlHandler.getCategoryList();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
