package ru.tonyhikas.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Intent currentIntent = getIntent();
        categoryId = currentIntent.getIntExtra("categoryId", 0);

        new GetQuestionsTask().execute(
                String.format(
                        "https://quiz.tonyhikas.ru/api/quiz/get_questions/?category_id=%d&questions_count=5",
                        categoryId
                ),
                "GET"
        );

    }

    private void drawQuestions(){
        questionsLayout = findViewById(R.id.questionsLayout);
        for (Question question: questions){
            LinearLayout questionLayout = new LinearLayout(this);
            questionLayout.setOrientation(LinearLayout.VERTICAL);

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

            questionsLayout.addView(questionLayout);
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
            progressDialog.setMessage("Ожидайте авторизации");
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

    protected class Answer{
        int id = 0;
        String text = "";
    }
    protected class Question{
        int id = 0;
        String text = "";
        ArrayList<Answer> answers = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();
    }
}