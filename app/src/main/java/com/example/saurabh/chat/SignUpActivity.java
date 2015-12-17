package com.example.saurabh.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final TextView username = (TextView) findViewById(R.id.txt_username);
        final TextView password = (TextView) findViewById(R.id.txt_password);
        final CheckBox rememberMe = (CheckBox) findViewById(R.id.checkbox_remember_me);

        final Button btnSignUp = (Button) findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String username_str = username.getText().toString();
                String password_str = password.getText().toString();

                if(username_str.isEmpty() || password_str.isEmpty()) {
                    return;
                }

                (new SignUpAsyncTask(username_str, password_str, rememberMe.isChecked())).execute();
            }
        });
    }

    class SignUpAsyncTask extends AsyncTask<String, String, JSONObject> {

        final String username, password;
        private int user_id;
        private final boolean rememberMe;

        public SignUpAsyncTask(String username, String password, boolean rememberMe) {
            this.username = username;
            this.password = password;
            this.rememberMe = rememberMe;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject input_json = new JSONObject();
            try {
                input_json.put("username", username);
                input_json.put("password", password);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            JSONParser jsonParser = new JSONParser();
            JSONObject output_json = jsonParser.getJSONFromUrl(WelcomeActivity.url + "/signup", input_json);
            Log.i("login", "output_json");

            return output_json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            boolean registered = false;
            if(json == null) return;
            try {
                registered = json.getBoolean("registered");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(!registered) {
                SignUpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Unable to register", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                return;
            }

            String session;

            try {
                session = json.getString("session");
                user_id = json.getInt("user_id");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            if(rememberMe) {
                SharedPreferences sharedPreferences =
                        getSharedPreferences("user", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("user_id", user_id)
                        .putString("username", username)
                        .putString("session", session).apply();
            }

            Intent menuIntent = new Intent(SignUpActivity.this, MenuActivity.class);
            menuIntent.putExtra("user_id", user_id);
            menuIntent.putExtra("username", username);
            menuIntent.putExtra("session", session);
            startActivity(menuIntent);
        }
    }
}
