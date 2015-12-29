package com.example.saurabh.chat.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.network.JSONParser;
import com.example.saurabh.chat.R;
import com.example.saurabh.chat.utilities.StatusLayout;
import com.example.saurabh.chat.utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class UserProfileActivity extends AppCompatActivity {

    int look_up_user_id;
    String url;
    JSONObject inputJSON;

    private boolean is_self = false;
    private boolean is_friend = false;
    private boolean has_requested_to_be_friends = false;

    RelativeLayout layoutDisplayUserProfile;
    ActionBar actionBar;
    TextView usernameDisplayText, displayJoinedDateText, displayLastActiveDateText, statusText;
    Button sendFriendRequestButton, blockUserButton;

    StatusLayout statusLayout;

    private static final int REFRESH_MILLIS = 30000;
    RefreshTimeAgoTimerTask refreshTimeAgoTimerTask;
    Date createdDate, lastActiveDate;

    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        res = getResources();

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
            if(look_up_user_id == ((ChatApplication) getApplication()).getUserID()) {
                is_self = true;
            }
        } else {
            look_up_user_id = ((ChatApplication) getApplication()).getUserID();
            is_self = true;
        }

        usernameDisplayText = (TextView) findViewById(R.id.txt_display_username);
        displayJoinedDateText = (TextView) findViewById(R.id.txt_display_joined_date);
        displayLastActiveDateText = (TextView) findViewById(R.id.txt_display_last_active);
        statusText = (TextView) findViewById(R.id.txt_display_status);
        sendFriendRequestButton = (Button) findViewById(R.id.btn_send_friend_request);
        blockUserButton = (Button) findViewById(R.id.btn_block_user);

        layoutDisplayUserProfile = (RelativeLayout) findViewById(R.id.layout_display_user_profile);

        layoutDisplayUserProfile.setVisibility(View.GONE);
        statusLayout = new StatusLayout(this);
        statusLayout.setLoading();

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

    @Override
    public void onPause() {
        // If initialised, cancel the timer to stop it from hogging resources
        if(refreshTimeAgoTimerTask != null) {
            refreshTimeAgoTimerTask.cancel();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        // If timer task has been initialised, it has been cancelled and
        // we need to create a new timer task to restart it.
        if(refreshTimeAgoTimerTask != null) {
            refreshTimeAgoTimerTask = new RefreshTimeAgoTimerTask();
        }

        super.onResume();
    }

    private class QueryUserAsyncTask extends AsyncTask<String, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(url + "/user/" + look_up_user_id, inputJSON);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject == null) {
                statusLayout.setError(String.format("Unable to query user ID '%s'", look_up_user_id));
                return;
            }

            try {
                usernameDisplayText.setText(jsonObject.getString("username"));
                statusText.setText(jsonObject.getBoolean("online") ? res.getString(R.string.status_online) : res.getString(R.string.status_offline));

                createdDate = Utility.parseDateAsUTC(jsonObject.getString("created_date"));
                displayJoinedDateText.setText(res.getString(R.string.joined_date, Utility.getTimeAgo(createdDate.getTime())));

                lastActiveDate = Utility.parseDateAsUTC(jsonObject.getString("last_active_date"));
                displayLastActiveDateText.setText(res.getString(R.string.last_active_date, Utility.getTimeAgo(lastActiveDate.getTime())));

                refreshTimeAgoTimerTask = new RefreshTimeAgoTimerTask();
            } catch (JSONException e) {
                e.printStackTrace();
                statusLayout.setError(String.format("Unable to query user ID '%s'", look_up_user_id));
                return;
            }

            layoutDisplayUserProfile.setVisibility(View.VISIBLE);
            statusLayout.hide();

            if(is_self) {
                sendFriendRequestButton.setVisibility(View.GONE);
                blockUserButton.setVisibility(View.GONE);
            } else if(is_friend) {
                sendFriendRequestButton.setVisibility(View.GONE);
            } else if(has_requested_to_be_friends) {
                sendFriendRequestButton.setText("Accept Friend Request");
            }
        }
    }

    private class RefreshTimeAgoTimerTask extends TimerTask {

        public RefreshTimeAgoTimerTask() {
            new Timer().scheduleAtFixedRate(this, REFRESH_MILLIS, REFRESH_MILLIS);
        }

        @Override
        public void run() {
            UserProfileActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayJoinedDateText.setText(res.getString(R.string.joined_date, Utility.getTimeAgo(createdDate.getTime())));
                    displayLastActiveDateText.setText(res.getString(R.string.last_active_date, Utility.getTimeAgo(lastActiveDate.getTime())));
                }
            });
        }
    }
}
