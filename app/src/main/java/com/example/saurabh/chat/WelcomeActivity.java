package com.example.saurabh.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends AppCompatActivity {

    public static final String url = "http://10.0.0.43:5000";
    private SharedPreferences userSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        final Button btnSignUp = (Button) findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        userSharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        if(userSharedPreferences.contains("username") && userSharedPreferences.contains("session")) {
            Intent chatIntent = new Intent(WelcomeActivity.this, MenuActivity.class);
            chatIntent.putExtra("returning user", true);
            startActivity(chatIntent);
        }
    }
}
