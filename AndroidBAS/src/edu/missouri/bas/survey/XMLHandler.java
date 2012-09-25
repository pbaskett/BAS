package edu.missouri.bas.survey;

import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;
import edu.missouri.bas.survey.answer.Answer;
import edu.missouri.bas.survey.answer.SurveyAnswer;
import edu.missouri.bas.survey.category.Category;
import edu.missouri.bas.survey.category.RandomCategory;
import edu.missouri.bas.survey.category.SurveyCategory;
import edu.missouri.bas.survey.question.CheckQuestion;
import edu.missouri.bas.survey.question.NumberQuestion;
import edu.missouri.bas.survey.question.QuestionType;
import edu.missouri.bas.survey.question.RadioInputQuestion;
import edu.missouri.bas.survey.question.RadioQuestion;
import edu.missouri.bas.survey.question.SurveyQuestion;
import edu.missouri.bas.survey.question.TextQuestion;


public class XMLHandler extends DefaultHandler {

	StringBuffer buffer = new StringBuffer();

	ArrayList<SurveyCategory> categories = new ArrayList<SurveyCategory>();
	
	SurveyCategory category;
	SurveyAnswer answer;
	SurveyQuestion question;
	ArrayList<SurveyAnswer> blockAnswerList;
	QuestionType questionType;
	String baseId;
	
	final String TAG = "Question handler";
	
	Context appContext;
	boolean external;
	
	public XMLHandler(Context c, boolean allowExternalXML, String baseId){
		this.appContext = c;
		this.external = allowExternalXML;
		if(baseId != null)
			this.baseId = baseId;
		else this.baseId = "";
	}
	
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attr){
		
		buffer.setLength(0);
		
		if(localName.equals("root")){
			//Do nothing
		}
		else if(localName.equals("category")){
			String s = attr.getValue("type");
			if(s != null && s.equals("random")){
				category = new RandomCategory();
			}
			else{
				category = new Category();
			}
		}
		else if(localName.equals("description")){
			//Do nothing
		}
		else if(localName.equals("question")){
			String s = attr.getValue("type");
			if(s == null) s = "invalid";
			String id = baseId;
			if(attr.getValue("id") != null)
				id += attr.getValue("id");
			if(s.equals("radio")){
				question = new RadioQuestion(id);
			}
			else if(s.equals("check")){
				question = new CheckQuestion(id);
			}
			else if(s.equals("number")){
				question = new NumberQuestion(id);
			}
			else if(s.equals("text")){
				question = new TextQuestion(id);
			}
			else if(s.equals("radioinput")){
				question = new RadioInputQuestion(id);
			}
			else{
				question = new CheckQuestion(id);
			}
		}
		else if(localName.equals("text")){
			//In a question block
			if(blockAnswerList != null){
				//Log.d(TAG,"Reading a question block question (<text>)");
				String id = baseId + attr.getValue("id");
				if(questionType.equals(QuestionType.RADIO)){
					//Log.d(TAG,"Question Block Type: radio");
					question = new RadioQuestion(id);
				}
				else if(questionType.equals(QuestionType.CHECKBOX)){
					//Log.d(TAG,"Question Block Type: check");
					question = new CheckQuestion(id);
				}
				else if(questionType.equals(QuestionType.NUMBER)){
					//Log.d(TAG,"Question Block Type: number");
					question = new NumberQuestion(id);
				}
				else if(questionType.equals(QuestionType.TEXT)){
					//Log.d(TAG,"Question Block Type: text");
					question = new TextQuestion(id);
				}
				else if(questionType.equals(QuestionType.RADIOINPUT)){
					question = new RadioInputQuestion(id);
				}
				else{
					//Log.d(TAG,"Question Block Type: default");
					question = new CheckQuestion(id);
				}
			}
		}
		else if(localName.equals("answer")){
			String id = attr.getValue("id");
			String skip = attr.getValue("skip");
			String action = attr.getValue("action");
			String triggerFile = attr.getValue("triggerFile");
			String triggerName = attr.getValue("triggerName");
			String triggerTimes = attr.getValue("triggerTimes");

			if(triggerFile != null &&
					triggerName != null &&
					triggerTimes != null){
				
				answer = new Answer(id, triggerName, triggerFile, triggerTimes);
			}
			else{
				answer = new Answer(id);
			}
			answer.setSkip(skip);
			if(action != null){
				if(action.equals("uncheck")){
					answer.setClear(true);
				}
				else if(action.equals("extrainput")){
					answer.setExtraInput(true);
				}
			}
		}
		else if(localName.equals("questionblock")){
			//Log.d(TAG,"Started a question block");
			blockAnswerList = new ArrayList<SurveyAnswer>();
			String type = attr.getValue("type");
			if(type.equals("radio")){
				//Log.d(TAG,"Question Block Type: radio");
				questionType = QuestionType.RADIO;
			}
			else if(type.equals("check")){
				//Log.d(TAG,"Question Block Type: check");
				questionType = QuestionType.CHECKBOX;
			}
			else if(type.equals("number")){
				//Log.d(TAG,"Question Block Type: number");
				questionType = QuestionType.NUMBER;
			}
			else if(type.equals("text")){
				//Log.d(TAG,"Question Block Type: text");
				questionType = QuestionType.TEXT;
			}
			else if(type.equals("radioinput")){
				//Log.d(TAG,"Question Block Type: text");
				questionType = QuestionType.RADIOINPUT;
			}
			else{
				//Log.d(TAG,"Question Block Type: defaulting to checkbox");
				questionType = QuestionType.CHECKBOX;
			}
		}
		else if(external && localName.equals("externalsource")){
			String fileName = attr.getValue("filename");
			String baseId = attr.getValue("baseid");
			if(fileName != null){
				XMLParser externalXML = new XMLParser();
				try {
					this.categories.addAll(externalXML.parseQuestion(
							new InputSource(appContext.getAssets().open(fileName)),
							appContext, false, baseId));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	
	}
	
	@Override 
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("root")){
			//Done with file
		}
		else if(localName.equals("category")){
			categories.add(category);
			category = null;
		}
		else if(localName.equals("description")){
			category.setQuestionText(buffer.toString());
		}
		else if(localName.equals("question")){ 
			//Log.d(TAG,"Finished question: "+question.getId());
			category.addQuestion(question);
			question = null;
		}
		else if(localName.equals("text")){
			question.setQuestion(buffer.toString());
			if(blockAnswerList != null){
				question.addAnswers(blockAnswerList);
				category.addQuestion(question);
				question = null;
			}
		}
		else if(localName.equals("answer")){
			answer.setAnswer(buffer.toString());
			//Log.d("XMLHandler","Read Answer: "+buffer.toString());
			if(blockAnswerList == null)
				question.addAnswer(answer);
			else{
				Log.d(TAG,"Finished question block answer list, length: "+blockAnswerList.size());
				blockAnswerList.add(answer);
			}
		}
		else if(localName.equals("questionblock")){
			blockAnswerList = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length){
		buffer.append(ch,start,length);
		//Log.d(TAG,"Got some characters");
	}
	
	public ArrayList<SurveyCategory> getCategoryList() {
		return categories;
	}
}
