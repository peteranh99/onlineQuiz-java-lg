package com.example.onlinequiz.Model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuestionInTest {
    private String questionId;
    private ArrayList<String> answerOrder = new ArrayList<>();
    private String userAnswer;
    private Question question;

    public QuestionInTest(){}

    public QuestionInTest(JSONObject jsonObject){
        try {
            setQuestionId(jsonObject.getString("QuestionId"));
            setUserAnswer(jsonObject.getString("UserAnswer"));
            // answer order
            String answerOrderString = jsonObject.getString("AnswerOrder");
            String[] answerOrderArray = answerOrderString.split("\\|");
            answerOrder.add(answerOrderArray[0]);
            answerOrder.add(answerOrderArray[1]);
            answerOrder.add(answerOrderArray[2]);
            answerOrder.add(answerOrderArray[3]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public QuestionInTest(String questionId, String answerOrder, String userAnswer){
        this.setQuestionId(questionId);
        //this.setAnswerOrder(answerOrder.split("\\|"));
        this.setUserAnswer(userAnswer);
    }

    public void loadQuestion(){
        Log.d("xxx", "load question");
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public ArrayList<String> getAnswerOrder() {
        return answerOrder;
    }

    public void setAnswerOrder(ArrayList<String> answerOrder) {
        this.answerOrder = answerOrder;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getInfo(){
        return questionId+"; "+answerOrder+"; "+userAnswer;
    }

    public JSONObject getJsonObject(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("QuestionId", questionId);
            obj.put("AnswerOrder", getSavingAnswerOrder());
            obj.put("UserAnswer", getUserAnswer());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private String getSavingAnswerOrder(){
        String divider = "|";
        String result = answerOrder.get(0)+divider+answerOrder.get(1)+divider+answerOrder.get(2)+divider+answerOrder.get(3);
        return result;
    }

}