package ru.tonyhikas.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegistrationActivity extends AppCompatActivity {

    Button sendCodeBtn;
    Button registerBtn;
    EditText emailField;
    EditText codeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        registerBtn = findViewById(R.id.registerBtn);
        emailField = findViewById(R.id.emailField);
        codeField = findViewById(R.id.codeField);

        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodeBtn.setEnabled(false);
                emailField.setEnabled(false);
            }
        });
    }
}