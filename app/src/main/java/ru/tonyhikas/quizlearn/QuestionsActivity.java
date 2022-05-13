package ru.tonyhikas.quizlearn;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class QuestionsActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    int categoryId;
    LinearLayout questionsLayout;
    ArrayList<Question> questions = new ArrayList<>();
    ArrayList<CheckedAnswer> checkedAnswers = new ArrayList<>();
    Button checkButton;
    Button returnButton;
    ArrayList<RadioButton> radioButtons = new ArrayList<>();

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        returnButton = findViewById(R.id.retrunBtn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        Intent currentIntent = getIntent();
        categoryId = currentIntent.getIntExtra("categoryId", 0);

        checkButton = findViewById(R.id.checkResult);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray data = new JSONArray();
                for (Question question: questions){
                    RadioGroup answersGroup = question.questionLayout.findViewWithTag(question.id);
                    int selectedId = answersGroup.getCheckedRadioButtonId();
                    if (selectedId == -1){
                        Toast.makeText(QuestionsActivity.this, "Вы ответили не на все вопросы", Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONObject answerElement = new JSONObject();
                    try {
                        answerElement.put("question_id", question.id);
                        answerElement.put("answer_id", selectedId);
                        data.put(answerElement);
                    }catch (JSONException e){
                        Toast.makeText(QuestionsActivity.this, "Ошибка сбора ответов", Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                new CheckQuestionsTask().execute(
                        "https://quiz.tonyhikas.ru/api/quiz/check_answers/",
                        "POST",
                        data.toString(),
                        token
                );
            }
        });

        new GetQuestionsTask().execute(
                String.format(
                        "https://quiz.tonyhikas.ru/api/quiz/get_questions/?category_id=%d&questions_count=5",
                        categoryId
                ),
                "GET",
                "",
                token
        );

    }

    private void drawQuestions(){
        questionsLayout = findViewById(R.id.questionsLayout);
        for (Question question: questions){
            // question layout
            LinearLayout questionLayout = new LinearLayout(this);
            questionLayout.setOrientation(LinearLayout.VERTICAL);
            questionLayout.setPadding(0, 20, 0, 20);

            // text
            TextView questionText = new TextView(this);
            questionText.setTextSize(20);
            questionText.setText(question.text);
            questionLayout.addView(questionText);

            //images
            for (String imageURL: question.images){
                ImageView image = new ImageView(this);
                new DownloadImageTask(image).execute(imageURL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                image.setLayoutParams(layoutParams);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setAdjustViewBounds(true);
                questionLayout.addView(image);
            }

            //answers
            RadioGroup answersGroup = new RadioGroup(this);
            answersGroup.setTag(question.id);
            for (Answer answer: question.answers){
                RadioButton answerButton = new RadioButton(this);
                answerButton.setText(answer.text);
                answerButton.setTextSize(20);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        125
                );
                answerButton.setLayoutParams(layoutParams);
                answerButton.setId(answer.id);
                radioButtons.add(answerButton);
                answersGroup.addView(answerButton);
            }
            questionLayout.addView(answersGroup);

            //separator
            View separator = new View(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    5
            );
            layoutParams.setMargins(0, 40, 0, 40);
            separator.setLayoutParams(layoutParams);
            separator.setBackgroundColor(Color.LTGRAY);
            questionLayout.addView(separator);


            question.questionLayout = questionLayout;
            questionsLayout.addView(questionLayout);
        }

    }

    private void drawChecked(){
        for (CheckedAnswer checkedAnswer: checkedAnswers){
            RadioButton rightButton = findViewById(checkedAnswer.right_answer_id);
            rightButton.setBackgroundColor(Color.GREEN);
            if (checkedAnswer.user_answer_id != 0){
                RadioButton userButton = findViewById(checkedAnswer.user_answer_id);
                userButton.setBackgroundColor(Color.RED);
            }
        }
    }

    private void disableAnswering(){
        checkButton.setVisibility(Button.GONE);
        returnButton.setVisibility(Button.VISIBLE);
        for (RadioButton radioButton: radioButtons){
            radioButton.setEnabled(false);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap imageMap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                imageMap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return imageMap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    protected class GetQuestionsTask extends RequestTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(QuestionsActivity.this);
            progressDialog.setMessage("Загрузка вопросов");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(ResponseResult result) {
            if (result.hasError){
                Toast.makeText(QuestionsActivity.this, "Ошибка при запросе: "+result.errorText, Toast.LENGTH_LONG).show();
            }
            else if (result.statusCode != 200){
                Toast.makeText(
                        QuestionsActivity.this,
                        String.format("Запрос завершился с кодом %d и ошибкой %s", result.statusCode, result.errorText),
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                try {
                    JSONArray questionsArray = new JSONArray(result.data);
                    for (int i = 0; i < questionsArray.length(); i++) {
                        Question question = new Question();
                        JSONObject questionElement = questionsArray.getJSONObject(i);
                        question.id = questionElement.getInt("id");
                        question.text = questionElement.getString("text");
                        JSONArray imagesArray = questionElement.getJSONArray("images");
                        for (int j = 0; j < imagesArray.length(); j++) {
                            question.images.add(imagesArray.getString(j));
                        }
                        JSONArray answersArray = questionElement.getJSONArray("answers");
                        for (int j = 0; j < answersArray.length(); j++) {
                            JSONObject answerElement = answersArray.getJSONObject(j);
                            Answer answer = new Answer();
                            answer.id = answerElement.getInt("id");
                            answer.text = answerElement.getString("text");
                            question.answers.add(answer);
                        }
                        questions.add(question);
                    }
                }catch (JSONException e){
                    Toast.makeText(QuestionsActivity.this, "Ошибка парсинга ответа", Toast.LENGTH_LONG).show();
                    return;
                }
                drawQuestions();
            }
            progressDialog.dismiss();
        }
    }

    protected class CheckQuestionsTask extends RequestTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(QuestionsActivity.this);
            progressDialog.setMessage("Проверка");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(ResponseResult result) {
            if (result.hasError){
                Toast.makeText(QuestionsActivity.this, "Ошибка при запросе: "+result.errorText, Toast.LENGTH_LONG).show();
            }
            else if (result.statusCode != 200){
                Toast.makeText(
                        QuestionsActivity.this,
                        String.format("Запрос завершился с кодом %d и ошибкой %s", result.statusCode, result.errorText),
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                try {
                    JSONObject resultElement = new JSONObject(result.data);
                    int total = resultElement.getInt("total");
                    int right = resultElement.getInt("right");
                    JSONArray rightAnswersArray = resultElement.getJSONArray("check_result");
                    for (int i = 0; i < rightAnswersArray.length(); i++) {
                        CheckedAnswer checkedAnswer = new CheckedAnswer();
                        JSONObject questionElement = rightAnswersArray.getJSONObject(i);
                        checkedAnswer.is_right = questionElement.getBoolean("is_right");
                        checkedAnswer.right_answer_id = questionElement.getInt("right_answer_id");
                        try{
                            checkedAnswer.user_answer_id = questionElement.getInt("user_answer_id");
                        }catch (JSONException e){
                            checkedAnswer.user_answer_id = 0;
                        }
                        checkedAnswers.add(checkedAnswer);
                    }
                }catch (JSONException e){
                    Toast.makeText(QuestionsActivity.this, "Ошибка парсинга ответа", Toast.LENGTH_LONG).show();
                    return;
                }
                disableAnswering();
                drawChecked();
            }
            progressDialog.dismiss();
        }
    }

    protected class Answer{
        int id = 0;
        String text = "";
    }

    protected class Question{
        int id = 0;
        String text = "";
        ArrayList<Answer> answers = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();

        LinearLayout questionLayout;

    }

    protected class CheckedAnswer{
        boolean is_right;
        int right_answer_id;
        int user_answer_id;
    }
}