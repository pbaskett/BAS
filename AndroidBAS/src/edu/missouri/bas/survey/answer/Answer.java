package edu.missouri.bas.survey.answer;

import android.util.Log;

public class Answer implements SurveyAnswer {
	
	protected String answerId;
	protected String value;
	protected String answerText;
	protected boolean selected = false;
	protected boolean clearOthers = false;
	protected String skipId;
	protected boolean extraInput = false;
	protected boolean hasTrigger = false;
	protected String triggerName = null;
	protected String triggerFile = null;
	protected long[] triggerTimes = null;
	
	public Answer(String id){
		this.answerId = id;
	}
	
	public Answer(String id, String triggerName, String triggerFile, String triggerTimes){
		this(id);
		this.triggerFile = triggerFile;
		this.triggerName = triggerName;
		this.hasTrigger = true;
		String[] splitTimes = triggerTimes.split(",");
		this.triggerTimes = new long[splitTimes.length];
		
		for(int i = 0; i < splitTimes.length; i++){
			long tempLong;
			try{
				tempLong = Integer.parseInt(splitTimes[i].replaceAll("[^0-9]", ""));
				Log.d("Answer","Trigger times: "+splitTimes[i].replaceAll("[^0-9]",""));
				if(splitTimes[i].contains("m")){
					tempLong *= (1000 * 60);
				}
				else if(splitTimes[i].contains("h")){
					tempLong *= (1000 * 60 * 60);
				}
				else if(splitTimes[i].contains("s")){
					tempLong *= (1000);
				}
				this.triggerTimes[i] = tempLong;
			} catch(NumberFormatException e){e.printStackTrace();}
		}
	}
	
	public Answer(String id, String value, String answerText){
		this.answerId = id;
		this.value = value;
		this.answerText = answerText;
	}
	
	public void setAnswer(String answerText){
		//this.answerText = answerText;
		this.value = answerText;
	}
	
	public String getId(){
		return this.answerId;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public String getAnswerText(){
		return this.answerText;
	}
	
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	public boolean isSelected(){
		return selected;
	}
	
	public void setClear(boolean clear){
		this.clearOthers = clear;
	}
	
	public boolean checkClear(){
		return clearOthers;
	}

	@Override
	public void setSkip(String id) {
		this.skipId = id;
	}

	@Override
	public String getSkip() {
		return skipId;
	}
	
	@Override
	public void setExtraInput(boolean extraInput){
		this.extraInput = extraInput;
	}
	
	@Override
	public boolean getExtraInput(){
		return this.extraInput;
	}

	@Override
	public void setSurveyTrigger(String name, String location, String times) {
		this.triggerFile = location;
		this.triggerName = name;
	}

	@Override
	public String getTriggerName() {
		return this.triggerName;
	}

	@Override
	public String getTriggerFile() {
		return this.triggerFile;
	}
	
	@Override 
	public long[] getTriggerTimes(){
		return this.triggerTimes;
	}
	
	@Override
	public boolean hasSurveyTrigger(){
		return hasTrigger;
	}
}
