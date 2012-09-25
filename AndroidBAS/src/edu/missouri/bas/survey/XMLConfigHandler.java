package edu.missouri.bas.survey;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XMLConfigHandler extends DefaultHandler {

	StringBuffer buffer = new StringBuffer();

	ArrayList<SurveyInfo> surveys = new ArrayList<SurveyInfo>();
	
	SurveyInfo currentSurvey;

	final String TAG = "Survey handler";
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attr){
		
		buffer.setLength(0);
		
		if(localName.equals("item")){
			String file = attr.getValue("file");
			String type = attr.getValue("type");
			String name = attr.getValue("name");
			currentSurvey = new SurveyInfo(file, type, name);
		}
	
	}
	
	@Override 
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("item")){
			currentSurvey.setDisplayName(buffer.toString());
			surveys.add(currentSurvey);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length){
		buffer.append(ch,start,length);
		Log.d(TAG,"Got some characters");
	}
	
	public ArrayList<SurveyInfo> getCategoryList() {
		return surveys;
	}
}
