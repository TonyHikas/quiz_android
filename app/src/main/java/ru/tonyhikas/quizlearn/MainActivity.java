package ru.tonyhikas.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button startBnt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBnt = findViewById(R.id.startBtn);

        startBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                // todo if already authed - go to category page
                if (true) {
                    intent = new Intent(MainActivity.this, CategoryActivity.class);
                }else{
                    intent = new Intent(MainActivity.this, RegistrationActivity.class);
                }
                startActivity(intent);
            }
        });
    }
}