package com.example.saurabh.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends AppCompatActivity {

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

        if(((ChatApplication) this.getApplication()).isLoggedIn()) {
            Intent chatIntent = new Intent(WelcomeActivity.this, MenuActivity.class);
            chatIntent.putExtra("returning user", true);
            startActivity(chatIntent);
        }
    }
}
