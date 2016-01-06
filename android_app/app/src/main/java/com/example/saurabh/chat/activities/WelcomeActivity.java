package com.example.saurabh.chat.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.R;
import com.example.saurabh.chat.network.JSONParser;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class WelcomeActivity extends AppCompatActivity implements Validator.ValidationListener {
    private static final String TAG = "WelcomeActivity";

    ChatApplication chatApplication;

    @NotEmpty
    private EditText url;
    @NotEmpty
    private EditText username;
    @NotEmpty
    private EditText password;
    private CheckBox rememberMe;

    private boolean signingUp = false;
    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        chatApplication = ((ChatApplication) WelcomeActivity.this.getApplication());

        if(chatApplication.isLoggedIn()) {
            Intent chatIntent = new Intent(WelcomeActivity.this, MenuActivity.class);
            chatIntent.putExtra("returning user", true);
            startActivity(chatIntent);
        }

        validator = new Validator(this);
        validator.setValidationListener(this);

        url = (EditText) findViewById(R.id.txt_server_url);
        username = (EditText) findViewById(R.id.txt_username);
        password = (EditText) findViewById(R.id.txt_password);
        rememberMe = (CheckBox) findViewById(R.id.checkbox_remember_me);

        final Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validator.validate();
            }
        });

        final Button btnSignUp = (Button) findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validator.validate();
                signingUp = true;
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        String username_str = username.getText().toString();
        String password_str = password.getText().toString();

        String url_str = url.getText().toString();
        if (!url_str.startsWith("https://") && !url_str.startsWith("http://")) {
            url_str = "http://" + url_str;
        }

        if (!url_str.matches(".*:([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$.*")) {
            url_str = url_str + ":5000";
        }

        Log.i(TAG, "Connecting to " + url_str);

        if(signingUp) {
            (new SignUpAsyncTask(url_str, username_str, password_str, rememberMe.isChecked())).execute();
            Toast.makeText(getApplicationContext(), "Signing up...", Toast.LENGTH_SHORT).show();
        } else {
            (new LoginAsyncTask(url_str, username_str, password_str, rememberMe.isChecked())).execute();
            Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    class SignUpAsyncTask extends AsyncTask<String, String, JSONObject> {

        final String url, username, password;
        private int user_id;
        private final boolean rememberMe;

        public SignUpAsyncTask(String url, String username, String password, boolean rememberMe) {
            this.url = url;
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
            JSONObject output_json = jsonParser.getJSONFromUrl(url + "/signup", input_json);
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
                WelcomeActivity.this.runOnUiThread(new Runnable() {
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

            chatApplication.setCredentials(url, user_id, username, session);

            if(rememberMe) {
                chatApplication.rememberCredentials();
            }

            Intent menuIntent = new Intent(WelcomeActivity.this, MenuActivity.class);
            startActivity(menuIntent);
        }
    }

    class LoginAsyncTask extends AsyncTask<String, String, JSONObject> {

        private int user_id;
        private final String url, username, password;
        private final boolean rememberMe;

        public LoginAsyncTask(String url, String username, String password, boolean rememberMe) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.rememberMe = rememberMe;
        }

        public void showServerError() {
            Toast.makeText(
                    getApplicationContext(),
                    "Unable to login; please try again later",
                    Toast.LENGTH_LONG
            ).show();
        }

        public void showInvalidAuthError() {
            Toast.makeText(
                    getApplicationContext(),
                    "Invalid username or password",
                    Toast.LENGTH_LONG
            ).show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject input_json = new JSONObject();
            try {
                input_json.put("username", username);
                input_json.put("password", password);
            } catch(JSONException e) {
                e.printStackTrace();
                return null;
            }

            JSONParser jsonParser = new JSONParser();

            JSONObject output_json = jsonParser.getJSONFromUrl(
                    url + "/login",
                    input_json
            );

            Log.i("login", "output_json");

            return output_json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            boolean authenticated;
            if(json == null) {
                showServerError();
                return;
            }

            try {
                authenticated = json.getBoolean("authenticated");
            } catch (JSONException e) {
                e.printStackTrace();
                showServerError();
                return;
            }

            if(!authenticated) {
                WelcomeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInvalidAuthError();
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
                showServerError();
                return;
            }

            chatApplication.setCredentials(url, user_id, username, session);

            if(rememberMe) {
                chatApplication.rememberCredentials();
            }

            Intent menuIntent = new Intent(WelcomeActivity.this, MenuActivity.class);
            startActivity(menuIntent);
        }
    }
}
