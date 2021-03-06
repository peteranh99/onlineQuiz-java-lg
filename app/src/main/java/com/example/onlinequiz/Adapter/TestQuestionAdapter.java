package com.example.onlinequiz.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.onlinequiz.Common.Helper;
import com.example.onlinequiz.Common.Message;
import com.example.onlinequiz.Model.Question;
import com.example.onlinequiz.Model.QuestionInTest;
import com.example.onlinequiz.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class TestQuestionAdapter extends BaseAdapter {
    private TextToSpeech tts;
    private Context context;
    private int layout;
    private ArrayList<QuestionInTest> questionInTestArrayList;

    public TestQuestionAdapter(Context context, int layout, ArrayList<QuestionInTest> questionInTestArrayList, TextToSpeech tts) {
        this.context = context;
        this.layout = layout;
        this.questionInTestArrayList = questionInTestArrayList;
        this.tts = tts;
    }


    @Override
    public int getCount() {
        return questionInTestArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    class ViewHolder {
        RelativeLayout rltMessage, rltMain, textAnswerContainer, pictureAnswerContainer, speechAnswerContainer, voiceAnswerContainer;
        TextView txtQuestion, txtA, txtB, txtC, txtD, txtMessage, txtVoiceAnswer, txtSpeechAnswer;
        ImageView imgA, imgB, imgC, imgD, imgStatus, imgQuestion, imgQuestionIcon, imgQuestionSpeaker;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TestQuestionAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new TestQuestionAdapter.ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);

            mapping(holder, convertView);
            convertView.setTag(holder);
        } else
            holder = (TestQuestionAdapter.ViewHolder) convertView.getTag();

        QuestionInTest questionInTest = questionInTestArrayList.get(position);
        int no = position + 1;
        if (questionInTest.getQuestion() != null) {
            // display question
            displayQuestion(position, questionInTest, holder);

            // display answer
            displayAnswer(questionInTest, holder);

            // set status image
            setStatusImage(isCorrect(questionInTest), holder);
            holder.rltMain.setVisibility(View.VISIBLE);
            holder.rltMessage.setVisibility(View.GONE);
        } else {
            String message = no + ". " + Message.failedToDisplayQuestion;
            holder.txtMessage.setText(message);
            holder.rltMessage.setVisibility(View.VISIBLE);
            holder.rltMain.setVisibility(View.GONE);
        }
        return convertView;
    }


    private boolean isCorrect(QuestionInTest questionInTest) {
        boolean result;
        Question question = questionInTest.getQuestion();
        if (question.getIsVoiceAnswer().equals("true")) {
            result = questionInTest.isVoiceAnswerCorrect();
        } else {
            result = (questionInTest.getUserAnswer().equals(questionInTest.getQuestion().getCorrectAnswer()));
        }

        return result;
    }

    // DISPLAY QUESTION
    private void displayQuestion(int index, QuestionInTest questionInTest, ViewHolder holder) {
        //displayQuestionIcon(questionInTest, holder);
        Question question = questionInTest.getQuestion();
        if (question.getIsImageQuestion().equals("true")) {
            Picasso.with(context)
                    .load(question.getQuestion())
                    .into(holder.imgQuestion);
        } else if (question.getIsVoiceAnswer().equals("true") || question.getIsAudioQuestion().equals("true")) {
            initOnSpeakerClickEvents(questionInTest, holder);
            holder.txtVoiceAnswer.setText(question.getQuestion());
        } else {
            String questionString = (index + 1) + ". " + questionInTest.getQuestion().getQuestion();
            holder.txtQuestion.setText(questionString);
        }
        solveQuestionVisibility(question, holder);
    }

    private void initOnSpeakerClickEvents(QuestionInTest questionInTest, ViewHolder holder) {
        holder.imgQuestionSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int speechStatus = tts.speak(questionInTest.getQuestion().getQuestion(), TextToSpeech.QUEUE_FLUSH, null);
                if (speechStatus == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error in converting Text to Speech!");
                }
            }
        });
    }

    private void displayQuestionIcon(QuestionInTest questionInTest, ViewHolder holder) {
        if (questionInTest.getQuestion().getIsVoiceAnswer().equals("true"))
            holder.imgQuestionIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.black_mic));
        else
            holder.imgQuestionIcon.setImageDrawable(null);
    }

    private void solveQuestionVisibility(Question question, ViewHolder holder) {
        if (question.getIsImageQuestion().equals("true")) {
            holder.imgQuestion.setVisibility(View.VISIBLE);
            holder.txtQuestion.setVisibility(View.GONE);
            holder.voiceAnswerContainer.setVisibility((View.GONE));
        } else if (question.getIsVoiceAnswer().equals("true")||question.getIsAudioQuestion().equals("true")) {
            holder.voiceAnswerContainer.setVisibility((View.VISIBLE));
            holder.txtQuestion.setVisibility(View.GONE);
            holder.imgQuestion.setVisibility(View.GONE);
        } else {
            holder.txtQuestion.setVisibility(View.VISIBLE);
            holder.imgQuestion.setVisibility(View.GONE);
            holder.voiceAnswerContainer.setVisibility((View.GONE));
        }
    }

    // DISPLAY ANSWER
    private void displayAnswer(QuestionInTest questionInTest, ViewHolder holder) {
        if (questionInTest.getQuestion().getIsImageAnswer().equals("true")) {
            displayPictureAnswer(questionInTest, holder);
        } else if (questionInTest.getQuestion().getIsVoiceAnswer().equals("true")) {
            displaySpeechAnswer(questionInTest, holder);
        } else {
            displayTextAnswer(questionInTest, holder);
        }
        solveAnswerContainerVisible(questionInTest, holder);
    }

    // SPEECH
    private void displaySpeechAnswer(QuestionInTest questionInTest, ViewHolder holder) {
        holder.txtSpeechAnswer.setText(Helper.ucFirst(questionInTest.getUserAnswer()));
        // format
        if (questionInTest.isVoiceAnswerCorrect(questionInTest.getQuestion(), questionInTest.getUserAnswer())) {
            // correct
            holder.txtSpeechAnswer.setTextColor(context.getResources().getColor(R.color.light_success));

        } else {
            // wrong
            holder.txtSpeechAnswer.setTextColor(context.getResources().getColor(R.color.danger));
        }
    }

    // TEXT
    private void displayTextAnswer(QuestionInTest questionInTest, ViewHolder holder) {
        ArrayList<String> answerOrder = questionInTest.getAnswerOrder();
        Question question = questionInTest.getQuestion();
        displayTextAnswerByLetter(holder.txtA, question.getAnswerByLetter(answerOrder.get(0)), questionInTest);
        displayTextAnswerByLetter(holder.txtB, question.getAnswerByLetter(answerOrder.get(1)), questionInTest);
        displayTextAnswerByLetter(holder.txtC, question.getAnswerByLetter(answerOrder.get(2)), questionInTest);
        displayTextAnswerByLetter(holder.txtD, question.getAnswerByLetter(answerOrder.get(3)), questionInTest);
    }

    @SuppressLint("ResourceAsColor")
    private void displayTextAnswerByLetter(TextView txt, String answer, QuestionInTest questionInTest) {
        Question question = questionInTest.getQuestion();

        // format background
        int backgroundDrawable;
        String textColorHexCode = null;
        if (isCorrectAnswerSelected(questionInTest, answer)) {
            backgroundDrawable = R.drawable.choose_correct_test_quetsion_answer_button;
        } else if (questionInTest.getUserAnswer().equals(answer))
            // choose wrong answer
            backgroundDrawable = R.drawable.choose_wrong_test_quetsion_answer_button;
        else if (question.getCorrectAnswer().equals(answer))
            // correct answer
            backgroundDrawable = R.drawable.correct_test_quetsion_answer_button;
        else {
            textColorHexCode = "#757575";
            backgroundDrawable = R.drawable.test_quetsion_answer_button;
        }
        // set text color
        if (textColorHexCode != null) txt.setTextColor(Color.parseColor(textColorHexCode));
        else txt.setTextColor(Color.WHITE);

        // set background
        txt.setBackground(ContextCompat.getDrawable(context, backgroundDrawable));

        // set text
        txt.setText(answer);
    }

    // PICTURE
    private void displayPictureAnswer(QuestionInTest questionInTest, ViewHolder holder) {
        Question question = questionInTest.getQuestion();
        ArrayList<String> answerOrder = questionInTest.getAnswerOrder();
        displayPictureAnswerByLetter(holder.imgA, question.getAnswerByLetter(answerOrder.get(0)), questionInTest);
        displayPictureAnswerByLetter(holder.imgB, question.getAnswerByLetter(answerOrder.get(1)), questionInTest);
        displayPictureAnswerByLetter(holder.imgC, question.getAnswerByLetter(answerOrder.get(2)), questionInTest);
        displayPictureAnswerByLetter(holder.imgD, question.getAnswerByLetter(answerOrder.get(3)), questionInTest);
    }

    private void displayPictureAnswerByLetter(ImageView img, String answer, QuestionInTest questionInTest) {
        Question question = questionInTest.getQuestion();
        Picasso.with(context)
                .load(answer)
                .into(img);

        // format
        int backgroundDrawable;
        if (isCorrectAnswerSelected(questionInTest, answer))
            backgroundDrawable = R.drawable.quize_light_success;
        else if (questionInTest.getUserAnswer().equals(answer))
            // choose wrong answer
            backgroundDrawable = R.drawable.quize_danger;
        else if (question.getCorrectAnswer().equals(answer))
            // correct answer
            backgroundDrawable = R.drawable.quize_blue;
        else {
            backgroundDrawable = R.drawable.quize_light;
        }

        // set background
        img.setBackground(ContextCompat.getDrawable(context, backgroundDrawable));


    }

    private void setStatusImage(boolean isCorrect, ViewHolder holder) {
        if (isCorrect) {
            holder.imgStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.correct));
        } else
            holder.imgStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.incorrect));
    }

    private void solveAnswerContainerVisible(QuestionInTest questionInTest, ViewHolder holder) {
        if (questionInTest.getQuestion().getIsImageAnswer().equals("true")) {
            holder.pictureAnswerContainer.setVisibility(View.VISIBLE);
            holder.textAnswerContainer.setVisibility(View.GONE);
            holder.speechAnswerContainer.setVisibility(View.GONE);
        } else if (questionInTest.getQuestion().getIsVoiceAnswer().equals("true")) {
            holder.speechAnswerContainer.setVisibility(View.VISIBLE);
            holder.textAnswerContainer.setVisibility(View.GONE);
            holder.pictureAnswerContainer.setVisibility(View.GONE);
        } else {
            holder.textAnswerContainer.setVisibility(View.VISIBLE);
            holder.pictureAnswerContainer.setVisibility(View.GONE);
            holder.speechAnswerContainer.setVisibility(View.GONE);
        }
    }

    boolean isCorrectAnswerSelected(QuestionInTest questionInTest, String answer) {
        Question question = questionInTest.getQuestion();
        return (question.getCorrectAnswer().equals(questionInTest.getUserAnswer())) && (question.getCorrectAnswer().equals(answer));
    }

    private void mapping(ViewHolder holder, View v) {
        holder.txtQuestion = (TextView) v.findViewById(R.id.txtQuestion);
        holder.txtA = (TextView) v.findViewById(R.id.txtA);
        holder.txtB = (TextView) v.findViewById(R.id.txtB);
        holder.txtC = (TextView) v.findViewById(R.id.txtC);
        holder.txtD = (TextView) v.findViewById(R.id.txtD);

        holder.imgA = (ImageView) v.findViewById(R.id.imgA);
        holder.imgB = (ImageView) v.findViewById(R.id.imgB);
        holder.imgC = (ImageView) v.findViewById(R.id.imgC);
        holder.imgD = (ImageView) v.findViewById(R.id.imgD);
        holder.imgStatus = (ImageView) v.findViewById(R.id.imgStatus);
        holder.imgQuestion = (ImageView) v.findViewById(R.id.imgQuestion);
        holder.imgQuestionIcon = (ImageView) v.findViewById(R.id.imgQuestionIcon);
        holder.imgQuestionSpeaker = (ImageView) v.findViewById(R.id.imgQuestionSpeaker);

        holder.txtVoiceAnswer = (TextView) v.findViewById(R.id.txtVoiceAnswer);
        holder.txtSpeechAnswer = (TextView) v.findViewById(R.id.txtSpeechAnswer);

        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.rltMessage = (RelativeLayout) v.findViewById(R.id.rltMessage);
        holder.rltMain = (RelativeLayout) v.findViewById(R.id.rltMain);
        holder.textAnswerContainer = (RelativeLayout) v.findViewById(R.id.textAnswerContainer);
        holder.pictureAnswerContainer = (RelativeLayout) v.findViewById(R.id.pictureAnswerContainer);
        holder.voiceAnswerContainer = (RelativeLayout) v.findViewById(R.id.voiceAnswerContainer);
        holder.speechAnswerContainer = (RelativeLayout) v.findViewById(R.id.speechAnswerContainer);
    }
}
