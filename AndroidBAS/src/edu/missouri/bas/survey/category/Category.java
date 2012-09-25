package edu.missouri.bas.survey.category;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.bas.survey.question.SurveyQuestion;


public class Category implements SurveyCategory{
	
	protected ArrayList<SurveyQuestion> questions;
	protected int nextQuestionNumber = 0;
	protected String questionText;
	
	public Category(){
		questions = new ArrayList<SurveyQuestion>();
	}
	
	public Category(String questionText){
		this.questionText = questionText;
		questions = new ArrayList<SurveyQuestion>();
	}
	
	public Category(String questionText, ArrayList<SurveyQuestion> questions){
		this.questionText = questionText;
		this.questions = new ArrayList<SurveyQuestion>();
		addQuestions(questions);
	}
	
	public Category(String questionText, SurveyQuestion[] questions){
		this.questionText = questionText;
		this.questions = new ArrayList<SurveyQuestion>();
		addQuestions(questions);
	}	
	
	@Override
	public SurveyQuestion nextQuestion(){
		if((nextQuestionNumber) >= questions.size()){
			return null;
		}
		return questions.get(nextQuestionNumber++);
	}
	
	@Override
	public SurveyQuestion getQuestion(int index){
		if(index >= questions.size()){
			return null;
		}
		return questions.get(index);
	}
	
	@Override
	public void addQuestion(SurveyQuestion question){
		questions.add(question);
	}
	
	@Override
	public void addQuestions(ArrayList<SurveyQuestion> newQuestions){
		questions.addAll(newQuestions);
	}
	
	@Override
	public void addQuestions(SurveyQuestion[] newQuestions){
		for(SurveyQuestion q: newQuestions){
			questions.add(q);
		}
	}
	
	@Override
	public String getQuestionText(String question){
		return questionText;
	}
	
	@Override
	public void setQuestionText(String question){
		this.questionText = question;
	}

	@Override
	public int totalQuestions() {
		return questions.size();
	}

	@Override
	public int currentQuestion() {
		return nextQuestionNumber;
	}
	
	@Override
	public List<SurveyQuestion> getQuestions(){
		return questions;
	}
	
}
