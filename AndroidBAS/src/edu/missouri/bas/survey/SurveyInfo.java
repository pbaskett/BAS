package edu.missouri.bas.survey;

public class SurveyInfo {
	protected String surveyName;
	protected String surveyFile;
	protected String surveyType;
	protected String surveyDisplayName;
	
	public SurveyInfo(String surveyFile, String surveyType, String surveyName){
		this.surveyFile = surveyFile;
		this.surveyType = surveyType;
		this.surveyName = surveyName;
	}
	
	public void setDisplayName(String name){
		this.surveyDisplayName = name;
	}
	
	public String getDisplayName(){
		return this.surveyDisplayName;
	}
	
	public String getName(){
		return this.surveyName;
	}
	
	public String getFileName(){
		return this.surveyFile;
	}
}
