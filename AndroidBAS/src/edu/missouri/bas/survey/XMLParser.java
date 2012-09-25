package edu.missouri.bas.survey;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import edu.missouri.bas.survey.category.SurveyCategory;

public class XMLParser {
	
	private XMLReader initializeReader() throws SAXException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		return reader;
	}
	
	public ArrayList<SurveyCategory> parseQuestion(InputSource XML,
			Context c, boolean allowExternalXML, String baseId){
		try{
			XMLReader reader = initializeReader();
			
			XMLHandler questionHandler = new XMLHandler(c, allowExternalXML, baseId);
			
			reader.setContentHandler(questionHandler);
			reader.parse(XML);		
			
			return questionHandler.getCategoryList();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
