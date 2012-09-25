package edu.missouri.bas.survey.question;

import java.util.ArrayList;
import java.util.Map;


import android.R;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.missouri.bas.survey.answer.SurveyAnswer;

public class CheckQuestion extends Question{

	boolean answered;
	
	public CheckQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.CHECKBOX;
	}
	
	@Override
	public LinearLayout prepareLayout(Context c) {
		
		LinearLayout layout = new LinearLayout(c);
		layout.setOrientation(LinearLayout.VERTICAL);
		TextView questionText = new TextView(c);
		questionText.setText(getQuestion());
		questionText.setTextAppearance(c, R.attr.textAppearanceLarge);

		layout.addView(questionText);
		for(SurveyAnswer ans: this.answers){
			CheckBox temp = new CheckBox(c);
			temp.setText(ans.getValue());
			answerViews.put(temp, ans);
			temp.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					SurveyAnswer a = answerViews.get(buttonView);
					if(isChecked){
						a.setSelected(true);
						if(a.checkClear()){
							for(Map.Entry<View, SurveyAnswer> entry: answerViews.entrySet()){
								if(!entry.getValue().equals(a)){
									((CheckBox)entry.getKey()).setChecked(false);
									entry.getValue().setSelected(false);
								}
							}
							for(Map.Entry<View, SurveyAnswer> entry: answerViews.entrySet()){
								if(!entry.getValue().equals(a)){
									((CheckBox)entry.getKey()).setEnabled(false);
								}
							}
						}
					}
					else{
						a.setSelected(false);
						if(a.checkClear()){
							for(Map.Entry<View, SurveyAnswer> entry: answerViews.entrySet()){
								((CheckBox)entry.getKey()).setEnabled(true);
							}
						}
					}
				}
			});
			layout.addView(temp);
		}
		
		return layout;
	}

	@Override
	public boolean validateSubmit() {
		boolean b = false;
		for(SurveyAnswer answer: answers){
			b = b | answer.isSelected();
		}
		return b;
	}
	
	@Override
	public ArrayList<String> getSelectedAnswers(){
		ArrayList<String> temp = new ArrayList<String>();
		for(SurveyAnswer answer: answers){
			if(answer.isSelected())
				temp.add(answer.getId());
		}
		return temp;
	}
	
	public String getSkip(){
		return null;
	}
}