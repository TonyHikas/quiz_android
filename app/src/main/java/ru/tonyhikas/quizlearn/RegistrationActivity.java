package ru.tonyhikas.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
                sendCodeBtn.setEnabled(false);
                emailField.setEnabled(false);
                new SendCodeTask().execute(
                        "https://quiz.tonyhikas.ru/api/auth?email=",
                        "GET"
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
            }
            else if (result.statusCode == 429) {
                Toast.makeText(RegistrationActivity.this, R.string.wait_resend_code, Toast.LENGTH_LONG).show();
            }
            else if (result.statusCode != 200){
                Toast.makeText(
                        RegistrationActivity.this,
                        String.format("Запрос завершился с кодом %d", result.statusCode),
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                codeField.setEnabled(true);
                registerBtn.setEnabled(true);
            }
            progressDialog.dismiss();
        }
    }
}