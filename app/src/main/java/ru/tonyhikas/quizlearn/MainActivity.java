package ru.tonyhikas.quizlearn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button startBnt;
    private Button logoutBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBnt = findViewById(R.id.startBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        startBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                // todo if already authed - go to category page
                SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "");
                if (token.equals("")) {
                    intent = new Intent(MainActivity.this, RegistrationActivity.class);
                }else{
                    intent = new Intent(MainActivity.this, CategoryActivity.class);
                }
                startActivity(intent);
            }
        });

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        if (!prefs.getString("token", "").equals("")){
            logoutBtn.setVisibility(Button.VISIBLE);
        }
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("token", "");
                editor.apply();
                logoutBtn.setVisibility(Button.GONE);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        if (!prefs.getString("token", "").equals("")){
            logoutBtn.setVisibility(Button.VISIBLE);
        }
    }
}