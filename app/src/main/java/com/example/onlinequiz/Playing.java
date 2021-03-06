package com.example.onlinequiz;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.onlinequiz.Common.Common;
import com.example.onlinequiz.Common.Helper;
import com.example.onlinequiz.Common.Message;
import com.example.onlinequiz.Common.ModelTag;
import com.example.onlinequiz.Database.UserModel;
import com.example.onlinequiz.Fragment.VoiceFragment;
import com.example.onlinequiz.Interface.ICallback;
import com.example.onlinequiz.Interface.IFragmentCommunicate;
import com.example.onlinequiz.Model.Question;
import com.example.onlinequiz.Model.QuestionInTest;
import com.example.onlinequiz.Model.Test;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class Playing extends Activity implements View.OnClickListener, ICallback<UserModel>, IFragmentCommunicate {

    FrameLayout frameVoiceAnswer;
    RelativeLayout rltMain, pictureAnswerContainer, voiceAnswerContainer;
    LinearLayout textAnswerContainer;
    ProgressBar progressBar;
    ImageView question_image;
    Button btnA, btnB, btnC, btnD, btnNext;
    VoiceFragment voiceFragment;

    ImageView imgA, imgB, imgC, imgD, imgAudio;
    TextView txtScore, txtQuestionNum, question_text, txtCaption;
    MediaPlayer correctAnswerMp3;
    MediaPlayer wrongAnswerMp3;
    MediaPlayer backgroundMp3;
    final static long INTERVAL = 100;
    int progressValue = 0;
    int index = 0, score = 0, thisQuestion = 0, correctAnswer;

    CountDownTimer mCountDown;

    Test test;
    QuestionInTest questionInTest;
    UserModel userModel;
    FragmentManager fragmentManager;
    boolean isForceStopListening = false;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();
        setContentView(R.layout.activity_playing);
        userModel = new UserModel(this);
        initTest();
        mapping();
        initEvents();
        initInternetStatusFragment();
        initVoiceAnswerFragment();

        // start background music
        backgroundMp3 = MediaPlayer.create(this, R.raw.wii);
        backgroundMp3.start();
        initTextToSpeech();
        startTest();
    }

    private void initTextToSpeech() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = tts.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(Playing.this, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initVoiceAnswerFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        voiceFragment = new VoiceFragment();
        fragmentTransaction.replace(R.id.frameVoiceAnswer, voiceFragment, "voice-answer");
        fragmentTransaction.commit();
    }

    private void initTest() {
        test = new Test();
    }

    // MEDIA PLAYER
    private void initMp3() {
        try {
            correctAnswerMp3.reset();
            wrongAnswerMp3.reset();
        } catch (Exception e) {
        }
        correctAnswerMp3 = MediaPlayer.create(this, R.raw.correct);
        wrongAnswerMp3 = MediaPlayer.create(this, R.raw.wrong);
    }

    private void playCorrectSound() {
        initMp3();
        correctAnswerMp3.start();
    }

    private void playIncorrectSound() {
        initMp3();
        wrongAnswerMp3.start();
    }
    //

    private void initEvents() {
        btnA.setOnClickListener(this);
        btnB.setOnClickListener(this);
        btnC.setOnClickListener(this);
        btnD.setOnClickListener(this);

        imgA.setOnClickListener(this);
        imgB.setOnClickListener(this);
        imgC.setOnClickListener(this);
        imgD.setOnClickListener(this);
        initBtnNextClick();
        initImgAudioClick();
    }

    private void initBtnNextClick() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountDown.cancel();
                solveTimeout();
            }
        });
    }

    private void initImgAudioClick() {
        imgAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(getCurrentQuestion().getQuestion());
            }
        });
    }

    private void showNextQuestion() {
        showQuestion(++index);
    }

    // ON TEXT/PICTURE ANSWER SELECTED
    @Override
    public void onClick(View v) {
        mCountDown.cancel();
        if (index < getTotalQuestion()) {
            String userAnswer;

            // get user answer
            if (getCurrentQuestion().getIsImageAnswer().equals("true")) {
                ArrayList<Integer> idArrayList = new ArrayList<>();
                idArrayList.add(R.id.imgA);
                idArrayList.add(R.id.imgB);
                idArrayList.add(R.id.imgC);
                idArrayList.add(R.id.imgD);
                int index = idArrayList.indexOf(v.getId());
                String letter = questionInTest.getAnswerOrder().get(index);
                userAnswer = getCurrentQuestion().getAnswerByLetter(letter);
            } else {
                Button clickedButton = (Button) v;
                userAnswer = clickedButton.getText().toString();
            }
            solveAnswerSelected(userAnswer);
        } else onDone();
    }

    private void solveAnswerSelected(String userAnswer) {
        questionInTest.setUserAnswer(userAnswer);
        test.addQuestion(questionInTest);
        if (isCorrectAnswer(questionInTest.getUserAnswer())) {
            playCorrectSound();
            score += 10;
            correctAnswer++;
        } else {
            playIncorrectSound();
        }
        showNextQuestion();
        displayScore();
    }

    private void displayScore() {
        txtScore.setText(String.format("%d", score));
    }

    private boolean isCorrectAnswer(String answer) {
        boolean result = false;
        Question question = getCurrentQuestion();

        switch (question.getAnswerType()) {
            case "picture":
            case "text":
                result = (answer.trim().equals(question.getCorrectAnswer().trim()));
                break;
            case "voice":
                result = QuestionInTest.isVoiceAnswerCorrect(question, answer);
                break;
        }
        return result;
    }

    // SHOW QUESTION
    private void showQuestion(int index) {
        isForceStopListening = false;
        if (index < getTotalQuestion()) {
            thisQuestion++;
            setViewVisibility(getCurrentQuestion().getQuestionType(), getCurrentQuestion().getAnswerType());
            solveBackgroundMusic();
            displayQuestionNum();
            resetProgress();
            displayQuestion();

            // RANDOM ANSWER
            displayAnswer();
            if (!getCurrentQuestion().getIsVoiceAnswer().equals("true"))
                voiceFragment.setPos(index);
            startCountdown();
        } else onDone();
    }

    private void solveBackgroundMusic() {
        if (getCurrentQuestion().getIsVoiceAnswer().equals("true") || getCurrentQuestion().getIsAudioQuestion().equals("true")) {
            if (backgroundMp3.isPlaying()) backgroundMp3.pause();
        } else {
            if (!backgroundMp3.isPlaying()) backgroundMp3.start();
        }
    }

    private void displayAnswer() {
        // generate random answer order. eg: ['b', 'c', 'd', 'a'];
        ArrayList<String> answerOrder = Question.genRandomAnswerOrder();

        // display answer with random order
        if (getCurrentQuestion().getIsImageAnswer().equals("true")) {
            // display picture answer
            displayPictureAnswer(answerOrder);

        } else if (getCurrentQuestion().getIsVoiceAnswer().equals("true")) {
        } else {
            // display text answer
            displayTextAnswer(answerOrder);
        }
        questionInTest = new QuestionInTest();

        // save random answer order to questionInTest  
        questionInTest.setAnswerOrder(answerOrder);
        questionInTest.setQuestionId(getCurrentQuestion().getId());
    }

    // TEXT ANSWER
    private void displayTextAnswer(ArrayList<String> answerOrder) {
        displayTextAnswerByButton(btnA, getCurrentQuestion().getAnswerByLetter(answerOrder.get(0)));
        displayTextAnswerByButton(btnB, getCurrentQuestion().getAnswerByLetter(answerOrder.get(1)));
        displayTextAnswerByButton(btnC, getCurrentQuestion().getAnswerByLetter(answerOrder.get(2)));
        displayTextAnswerByButton(btnD, getCurrentQuestion().getAnswerByLetter(answerOrder.get(3)));
    }

    private void displayTextAnswerByButton(Button button, String answer) {
        button.setText(answer);
    }

    // PICTURE ANSWER
    private void displayPictureAnswer(ArrayList<String> answerOrder) {
        displayPictureAnswerByImageView(imgA, getCurrentQuestion().getAnswerByLetter(answerOrder.get(0)));
        displayPictureAnswerByImageView(imgB, getCurrentQuestion().getAnswerByLetter(answerOrder.get(1)));
        displayPictureAnswerByImageView(imgC, getCurrentQuestion().getAnswerByLetter(answerOrder.get(2)));
        displayPictureAnswerByImageView(imgD, getCurrentQuestion().getAnswerByLetter(answerOrder.get(3)));
    }

    private void displayPictureAnswerByImageView(ImageView img, String answer) {
        Picasso.with(getBaseContext())
                .load(answer)
                .into(img);
    }

    private void displayQuestionNum() {
        txtQuestionNum.setText(String.format("%d / %d", thisQuestion, getTotalQuestion()));
    }
    //

    private void resetProgress() {
        progressBar.setProgress(0);
        progressValue = 0;
    }

    private void displayQuestion() {
        switch (getCurrentQuestion().getQuestionType()) {
            case "text":
                switch (getCurrentQuestion().getAnswerType()) {
                    case "picture":
                    case "text":
                        question_text.setText(getCurrentQuestion().getQuestion());
                        break;
                    case "voice":
                        txtCaption.setText(Message.speakThisSentence);
                        question_text.setText(getCurrentQuestion().getQuestion());
                        break;
                }
                break;
            case "audio":
                switch (getCurrentQuestion().getAnswerType()) {
                    case "picture":
                    case "text":
                        txtCaption.setText(Message.listenAndChooseTheCorrectAnswer);
                        break;
                    case "voice":
                        txtCaption.setText(Message.listenAndRepeat);
                        break;
                }
                break;
            case "picture":
                Picasso.with(getBaseContext())
                        .load(getCurrentQuestion().getQuestion())
                        .into(question_image);

                switch (getCurrentQuestion().getAnswerType()) {
                    case "picture":
                    case "text":
                        break;
                    case "voice":
                        txtCaption.setText(Message.whatIsThis);
                        break;
                }
                break;
        }
    }

    private void setViewVisibility(String questionType, String answerType) {
        setQuestionViewVisibility(questionType, answerType);
        setAnswerViewVisibility(answerType);
    }

    private void setQuestionViewVisibility(String questionType, String answerType) {
        switch (questionType) {
            case "text":
                switch (answerType) {
                    case "picture":
                    case "text":
                        question_text.setVisibility(View.VISIBLE);
                        question_image.setVisibility(View.GONE);
                        txtCaption.setVisibility(View.GONE);
                        break;
                    case "voice":
                        question_image.setVisibility(View.GONE);
                        txtCaption.setVisibility(View.VISIBLE);
                        question_text.setVisibility(View.VISIBLE);
                        break;
                }
                imgAudio.setVisibility(View.GONE);
                break;
            case "audio":
                imgAudio.setVisibility(View.VISIBLE);
                txtCaption.setVisibility(View.VISIBLE);
                question_image.setVisibility(View.GONE);
                question_text.setVisibility(View.GONE);
                break;
            case "picture":
                question_image.setVisibility(View.VISIBLE);
                question_text.setVisibility(View.GONE);
                imgAudio.setVisibility(View.GONE);
                switch (answerType) {
                    case "picture":
                    case "text":
                        txtCaption.setVisibility(View.GONE);
                        break;
                    case "voice":
                        txtCaption.setVisibility(View.VISIBLE);
                        break;
                }

                break;
        }
    }

    private void setAnswerViewVisibility(String answerType) {
        switch (answerType) {
            case "picture":
                pictureAnswerContainer.setVisibility(View.VISIBLE);
                textAnswerContainer.setVisibility(View.GONE);
                voiceAnswerContainer.setVisibility(View.GONE);
                break;
            case "text":
                textAnswerContainer.setVisibility(View.VISIBLE);
                pictureAnswerContainer.setVisibility(View.GONE);
                voiceAnswerContainer.setVisibility(View.GONE);
                break;
            case "voice":
                voiceAnswerContainer.setVisibility(View.VISIBLE);
                textAnswerContainer.setVisibility(View.GONE);
                pictureAnswerContainer.setVisibility(View.GONE);
                break;
        }
    }

    private void onDone() {
        mCountDown.cancel();
        test.setCategoryId(Common.categoryId);
        test.setNumberOfQuestion(getTotalQuestion());
        test.setScore(score);
        test.setDate(Helper.getCurrentISODateString());
        Common.getCurrentUser().getTestManager().add(test);
        userModel.updateCurrentUserTests(ModelTag.updateCurrentUserTests);

        Intent intent = new Intent(this, Done.class);
        Bundle dataSend = new Bundle();
        dataSend.putInt("SCORE", score);
        dataSend.putInt("TOTAL", getTotalQuestion());
        dataSend.putInt("CORRECTED", correctAnswer);
        intent.putExtras(dataSend);
        startActivity(intent);
        finish();
    }

    private void startTest() {
        showQuestion(index);
    }

    private void solveTimeout() {
        playIncorrectSound();
        if (getCurrentQuestion().getIsVoiceAnswer().equals("true"))
            voiceFragment.onStopListening();
        questionInTest.setUserAnswer("");
        test.addQuestion(questionInTest);
        showNextQuestion();
    }

    private void mapping() {
        voiceAnswerContainer = (RelativeLayout) findViewById(R.id.rltVoiceAnswerContainer);
        pictureAnswerContainer = (RelativeLayout) findViewById(R.id.pictureAnswerContainer);
        textAnswerContainer = (LinearLayout) findViewById(R.id.textAnswerContainer);
        frameVoiceAnswer = (FrameLayout) findViewById(R.id.frameVoiceAnswer);

        txtCaption = (TextView) findViewById(R.id.txtCaption);
        txtScore = (TextView) findViewById(R.id.txtScore);
        txtQuestionNum = (TextView) findViewById(R.id.txtTotalQuestion);
        question_text = (TextView) findViewById(R.id.question_text);
        question_image = (ImageView) findViewById(R.id.question_image);
        imgAudio = (ImageView) findViewById(R.id.imgAudio);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rltMain = (RelativeLayout) findViewById(R.id.rltMain);

        btnA = (Button) findViewById(R.id.btnAnswerA);
        btnB = (Button) findViewById(R.id.btnAnswerB);
        btnC = (Button) findViewById(R.id.btnAnswerC);
        btnD = (Button) findViewById(R.id.btnAnswerD);
        btnNext = (Button) findViewById(R.id.btnNext);

        imgA = (ImageView) findViewById(R.id.imgA);
        imgB = (ImageView) findViewById(R.id.imgB);
        imgC = (ImageView) findViewById(R.id.imgC);
        imgD = (ImageView) findViewById(R.id.imgD);
    }

    private int getTotalQuestion() {
        return Common.testQuestionQty;
    }

    // SUPPORTED METHOD
    private Question getCurrentQuestion() {
        return Common.questionsList.get(index);
    }

    @Override
    public void itemCallBack(UserModel item, String tag) {
    }

    @Override
    public void listCallBack(ArrayList<UserModel> items, String tag) {
    }

    // FRAGMENT COMMUNICATE
    @Override
    public void communicate(String value, String tag) {
        if (!isForceStopListening) {
            if (tag.equals(VoiceFragment.fragmentTag)) {
                String userAnswer = value;
                mCountDown.cancel();
                this.voiceFragment.setPos(index + 1);
                solveAnswerSelected(userAnswer);
            }
        } else {
            isForceStopListening = false;
        }
    }

    @Override
    public void communicate(JSONObject jsonObject, String tag) {
        try {
            String eventName = jsonObject.getString("eventName");
            if (tag.equals(VoiceFragment.fragmentTag)) {
                if (eventName.equals("onError")) onSpeechError();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSpeechError() {
        int pos = voiceFragment.getPos();
        if (pos == index || pos == -1) {
            this.voiceFragment.setPos(index + 1);
            String userAnswer = "";
            mCountDown.cancel();
            solveAnswerSelected(userAnswer);
        } else {
            this.voiceFragment.setPos(index);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountDown.cancel();
        backgroundMp3.reset();
    }

    private void startCountdown() {
        Question question = getCurrentQuestion();
        int timeout = question.getTimeLimit() * 1000;
        progressBar.setMax(timeout);
        if (mCountDown != null) mCountDown.cancel();
        mCountDown = new CountDownTimer(timeout, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(progressValue);
                progressValue += 100;
            }

            @Override
            public void onFinish() {
                mCountDown.cancel();
                solveTimeout();
            }
        }.start();
    }

    private void speak(String value) {
        int speechStatus = tts.speak(value, TextToSpeech.QUEUE_FLUSH, null);
        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }
}

