package com.example.saurabh.chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class UserProfileActivity extends AppCompatActivity {

    int look_up_user_id;
    String url;
    JSONObject inputJSON;

    TextView usernameDisplayText;
    TextView displayJoinedDateText;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        url = ((ChatApplication) getApplication()).getURL();

        inputJSON = new JSONObject();
        try {
            inputJSON.put("username", ((ChatApplication) getApplication()).getUsername());
            inputJSON.put("user_id", ((ChatApplication) getApplication()).getUserID());
            inputJSON.put("session", ((ChatApplication) getApplication()).getSession());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        if(intent.hasExtra("user_id")) {
            look_up_user_id = intent.getIntExtra("user_id", -1);
        } else {
            look_up_user_id = ((ChatApplication) getApplication()).getUserID();
        }

        usernameDisplayText = (TextView) findViewById(R.id.txt_display_username);
        displayJoinedDateText = (TextView) findViewById(R.id.txt_display_joined_date);

        (new QueryUserAsyncTask()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class QueryUserAsyncTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject outputJSON = new JSONParser().getJSONFromUrl(url + "/user/" + look_up_user_id, inputJSON);

            return outputJSON;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject == null) {
                Toast.makeText(UserProfileActivity.this, String.format("Unable to query user ID '%s'", look_up_user_id), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                usernameDisplayText.setText(jsonObject.getString("username"));

                Date userCreatedDate = Utility.parseDateAsUTC(jsonObject.getString("created_date"));
                displayJoinedDateText.setText("Joined " + Utility.getTimeAgo(userCreatedDate.getTime()));

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(UserProfileActivity.this, String.format("Unable to query user ID '%s'", look_up_user_id), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}
