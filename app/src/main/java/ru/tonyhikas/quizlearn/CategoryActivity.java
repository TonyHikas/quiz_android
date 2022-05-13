package ru.tonyhikas.quizlearn;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    Spinner categorySelect;
    Button startButton;

    ArrayList<Category> categories = new ArrayList<Category>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categorySelect = findViewById(R.id.categorySelect);
        startButton = findViewById(R.id.startTest);
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        new CategoryTask().execute(
                "https://quiz.tonyhikas.ru/api/quiz/category_autocomplete/?_type=query",
                "GET",
                "",
                token
        );

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int categoryId = categories.get(categorySelect.getSelectedItemPosition()).id;
                Intent intent = new Intent(CategoryActivity.this, QuestionsActivity.class);
                intent.putExtra("categoryId", categoryId);
                startActivity(intent);
            }
        });


    }

    protected class CategoryTask extends RequestTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CategoryActivity.this);
            progressDialog.setMessage("Ожидайте получения категорий");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(ResponseResult result) {
            if (result.hasError){
                Toast.makeText(CategoryActivity.this, "Ошибка при запросе: "+result.errorText, Toast.LENGTH_LONG).show();
            }
            else if (result.statusCode != 200){
                Toast.makeText(
                        CategoryActivity.this,
                        String.format("Запрос завершился с кодом %d и ошибкой %s", result.statusCode, result.errorText),
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                try {
                    JSONObject jsonObject = new JSONObject(result.data);
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject categoryElement = jsonArray.getJSONObject(i);
                        categories.add(
                                new Category(
                                        categoryElement.getInt("id"),
                                        categoryElement.getString("text")
                                )
                        );
                    }
                }catch (JSONException e){
                    Toast.makeText(CategoryActivity.this, "Ошибка парсинга ответа", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<String> arraySpinner = new ArrayList<>();
                for (Category category: categories)
                {
                    arraySpinner.add(category.text);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        CategoryActivity.this,
                        android.R.layout.simple_spinner_item,
                        arraySpinner
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySelect.setAdapter(adapter);
            }
            progressDialog.dismiss();
        }
    }

    private class Category{

        public int id;
        public String text;

        public Category(int id, String text){
            this.id = id;
            this.text = text;
        }
    }
}