package edu.missouri.bas.survey.question;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import edu.missouri.bas.survey.answer.SurveyAnswer;


public abstract class Question implements SurveyQuestion {

	protected ArrayList<SurveyAnswer> answers = new ArrayList<SurveyAnswer>();
	protected HashMap<View, SurveyAnswer> answerViews = new HashMap<View, SurveyAnswer>();
	protected String questionText;
	protected String questionId;
	protected QuestionType questionType;
	
	@Override
	public String getQuestion() {
		return questionText;
	}

	@Override
	public void setQuestion(String questionText) {
		this.questionText = questionText;
		
	}

	@Override
	public void addAnswer(SurveyAnswer answer) {
		this.answers.add(answer);
	}

	@Override
	public void addAnswers(ArrayList<SurveyAnswer> answers) {
		this.answers.addAll(answers);
	}

	@Override
	public void addAnswers(SurveyAnswer[] answers) {
		for(SurveyAnswer a: answers){
			this.answers.add(a);
		}
	}

	@Override
	public ArrayList<SurveyAnswer> getAnswers() {
		return answers;
	}

	@Override
	public void setQuestionType(QuestionType type) {
		this.questionType = type;
	}

	@Override
	public QuestionType getQuestionType() {
		return questionType;
	}

	@Override
	public abstract LinearLayout prepareLayout(Context c);

	@Override
	public abstract boolean validateSubmit();
	
	@Override
	public abstract String getSkip();
	
	@Override
	public String getId(){
		return questionId;
	}

}
