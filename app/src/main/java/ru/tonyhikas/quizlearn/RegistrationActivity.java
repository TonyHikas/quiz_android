package ru.tonyhikas.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends AppCompatActivity {

    Button sendCodeBtn;
    Button registerBtn;
    EditText emailField;
    EditText codeField;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        registerBtn = findViewById(R.id.registerBtn);
        emailField = findViewById(R.id.emailField);
        codeField = findViewById(R.id.codeField);
        codeField.setEnabled(false);
        registerBtn.setEnabled(false);

        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                if (email.equals("")){
                    Toast.makeText(RegistrationActivity.this, R.string.empty_email, Toast.LENGTH_LONG).show();
                    return;
                }
                sendCodeBtn.setEnabled(false);
                emailField.setEnabled(false);
                new SendCodeTask().execute(
                        "https://quiz.tonyhikas.ru/api/auth?email="+email,
                        "GET"
                );

            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeField.getText().toString();
                String email = emailField.getText().toString();
                if (code.equals("")){
                    Toast.makeText(RegistrationActivity.this, R.string.empty_code, Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject data;
                try {
                    data = new JSONObject();
                    data.put("email", email);
                    data.put("code", code);
                }catch (JSONException e){
                    Toast.makeText(RegistrationActivity.this, "Неверные данные в запросе", Toast.LENGTH_LONG).show();
                    return;
                }
                new RegisterTask().execute(
                        "https://quiz.tonyhikas.ru/api/auth/",
                        "POST",
                        data.toString()
                );

            }
        });
    }

    protected class SendCodeTask extends RequestTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RegistrationActivity.this);
            progressDialog.setMessage("Ожидайте отправки письма на электронную почту");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(ResponseResult result) {
            if (result.hasError){
                Toast.makeText(RegistrationActivity.this, "Ошибка при запросе: "+result.errorText, Toast.LENGTH_LONG).show();
                sendCodeBtn.setEnabled(true);
                emailField.setEnabled(true);
            }
            else if (result.statusCode == 429) {
                Toast.makeText(RegistrationActivity.this, R.string.wait_resend_code, Toast.LENGTH_LONG).show();
                sendCodeBtn.setEnabled(true);
                emailField.setEnabled(true);
            }
            else if (result.statusCode != 200){
                Toast.makeText(
                        RegistrationActivity.this,
                        String.format("Запрос завершился с кодом %d и ошибкой %s", result.statusCode, result.errorText),
                        Toast.LENGTH_LONG
                ).show();
                sendCodeBtn.setEnabled(true);
                emailField.setEnabled(true);
            }
            else {
                codeField.setEnabled(true);
                registerBtn.setEnabled(true);
            }
            progressDialog.dismiss();
        }
    }
    protected class RegisterTask extends RequestTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RegistrationActivity.this);
            progressDialog.setMessage("Ожидайте авторизации");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(ResponseResult result) {
            if (result.hasError){
                Toast.makeText(RegistrationActivity.this, "Ошибка при запросе: "+result.errorText, Toast.LENGTH_LONG).show();
            }
            else if (result.statusCode != 200){
                Toast.makeText(
                        RegistrationActivity.this,
                        String.format("Запрос завершился с кодом %d и ошибкой %s", result.statusCode, result.errorText),
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                String token;
                try {
                    JSONObject jsonObject = new JSONObject(result.data);
                    token = jsonObject.getString("token");
                }catch (JSONException e){
                    Toast.makeText(RegistrationActivity.this, "Ошибка парсинга ответа", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("token", token);
                editor.apply();
                Intent intent = new Intent(RegistrationActivity.this, CategoryActivity.class);
                startActivity(intent);
        }
            progressDialog.dismiss();
        }
    }
}