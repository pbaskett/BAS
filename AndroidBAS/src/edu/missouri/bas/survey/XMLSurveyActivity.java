package edu.missouri.bas.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import edu.missouri.bas.R;
import edu.missouri.bas.survey.category.RandomCategory;
import edu.missouri.bas.survey.category.SurveyCategory;
import edu.missouri.bas.survey.question.SurveyQuestion;




public class XMLSurveyActivity extends Activity {
	
	public static final String INTENT_ACTION_SURVEY_RESULTS = "action_survey_results";

	public static final String INTENT_EXTRA_SURVEY_NAME = "survey_name";

	public static final String INTENT_EXTRA_SURVEY_RESULTS = "survey_results";

	public static final String INTENT_EXTRA_COMPLETION_TIME = "survey_completion_time";

	ArrayList<SurveyCategory> cats = null;

	SurveyQuestion currentQuestion;
	SurveyCategory currentCategory;
	int categoryNum;
    LinearLayout surveyLayout;
    Button submitButton;
    String skipTo = null;
    
    String surveyName;
    String surveyFile;
    
    /*
     * Putting a serializable in an intent seems to default to the class
     * that implements serializable, so LinkedHashMap or TreeMap are treated
     * as a hashmap when received.
     * 
     * TODO: Maybe make a private class with LinkedHash/Tree Map + parcelable
     */
    LinkedHashMap<String, List<String>> answerMap;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_layout);
        
        answerMap = new LinkedHashMap<String, List<String>>();
        
        /*
         * The same submit button is used for every question.
         * New buttons could be made for each question if
         * additional specific functionality is needed/
         */
        submitButton = new Button(this);
        submitButton.setText("Submit");
        
        submitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(currentQuestion.validateSubmit()){
					ViewGroup vg = setupLayout(nextQuestion());
					if(vg != null)
						setContentView(vg);
				}
			}
        });
        
        //Setup XML parser
		XMLParser parser = new XMLParser();
		
		//Tell the parser which survey to use
		//TODO: Get this from starting intent...
		//surveyName = "morningReport.xml";
		
		surveyName = getIntent().getStringExtra("survey_name");
		surveyFile = getIntent().getStringExtra("survey_file");
		Log.d("XMLSurvey","File Name: "+surveyFile);
		//Open the specified survey
		try {
			/*
			 * .parseQuestion takes an input source to the assets file,
			 * a context in case there are external files, a boolean for
			 * allowing external files, and a baseid that will be appended
			 * to question ids.  If boolean is false, no context is needed.
			 */
			cats = parser.parseQuestion(new InputSource(getAssets().open(surveyFile)),
					this,true,"");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Survey didn't contain any categories
		if(cats == null){
			surveyComplete();
		}
		//Survey did contain categories
		else{
			//Set current category to the first category
			currentCategory = cats.get(0);
			//Setup the layout
			ViewGroup vg = setupLayout(nextQuestion());
			if(vg != null)
				setContentView(vg);
		}
    }
    
    protected ScrollView setupLayout(LinearLayout layout){
    	/* Didn't get a layout from nextQuestion(),
    	 * error (shouldn't be possible) or survey complete,
    	 * either way finish safely.
    	 */
    	if(layout == null){
    		surveyComplete();
    		return null;
    	}
    	else{
			//Setup ScrollView
			ScrollView sv = new ScrollView(getApplicationContext());
			//Remove submit button from its parent so we can reuse it
			if(submitButton.getParent() != null){
				((ViewGroup)submitButton.getParent()).removeView(submitButton);
			}
			//Add submit button to layout
			layout.addView(submitButton);
			//Add layout to scroll view in case it's too long
			sv.addView(layout);
			//Display scroll view
			setContentView(sv);
			return sv;
    	}
    }
    
    protected void surveyComplete(){
    	//Store
    		/*TODO: Doing this in service, 
    		 * move it here in case service fails? 
    		 * Will be slower because of i/o.
    		 */
    	for(SurveyCategory cat: cats){
    		for(SurveyQuestion question: cat.getQuestions()){
    			answerMap.put(question.getId(), question.getSelectedAnswers());
    		}
    	}
		answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());

    	
    	//Send to service
    	Intent surveyResultsIntent = new Intent();
    	surveyResultsIntent.setAction(INTENT_ACTION_SURVEY_RESULTS);
    	surveyResultsIntent.putExtra(INTENT_EXTRA_SURVEY_NAME, surveyName);
    	surveyResultsIntent.putExtra(INTENT_EXTRA_SURVEY_RESULTS, answerMap);
    	surveyResultsIntent.putExtra(INTENT_EXTRA_COMPLETION_TIME, System.currentTimeMillis());
    	this.sendBroadcast(surveyResultsIntent);
    	
    	//Alert user
    	Toast.makeText(this, "Survey Complete.", Toast.LENGTH_LONG).show();
    	
    	/* Finish, this call is asynchronous, so handle that when
    	 * views need to be changed...
    	 */
    	finish();
    }
    
    protected LinearLayout nextQuestion(){
    	SurveyQuestion temp = null;
    	boolean done = false;
    	boolean allowSkip = false;
    	do{
    		if(temp != null)
    			answerMap.put(temp.getId(), null);
    		//Simplest case: category has the next question
    		temp = currentCategory.nextQuestion();
    		
    		//Category is out of questions, try to move to next category
    		if(temp == null && (++categoryNum < cats.size())){
    			/* Advance the category.  Loop will get the question
    			 * on next iteration.
    			 */
    			currentCategory = cats.get(categoryNum);
    			if(currentCategory instanceof RandomCategory &&
    					currentQuestion.getSkip() != null){
    				//Check if skip is in category
    				RandomCategory tempCat = (RandomCategory) currentCategory;
    				if(tempCat.containsQuestion(currentQuestion.getSkip()))
    					allowSkip = true;
    			}
    		}
    		//Out of categories, survey must be done
    		else if(temp == null){
    			Log.d("XMLActivity","Should be done...");
    			done = true;
    			break;
    			//surveyComplete();
    		}
    	}while(temp == null ||
    			(currentQuestion != null && currentQuestion.getSkip() != null &&
    			!(currentQuestion.getSkip().equals(temp.getId()) || allowSkip))	);
		/*if(currentQuestion != null){
			answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());
		}*/
    	if(done){
    		//surveyComplete();
    		return null;
    	}
    	else{

    		currentQuestion = temp;
    		return currentQuestion.prepareLayout(this);
    	}
    }
}