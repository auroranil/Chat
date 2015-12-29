package com.example.saurabh.chat.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.saurabh.chat.ChatApplication;

import org.json.JSONException;
import org.json.JSONObject;

public class Logout {
    Activity activity;

    public Logout(final Activity activity, final int user_id, final String username, final String session) {
        this.activity = activity;
        AlertDialog.Builder logoutConfirmAlertDialogBuilder = new AlertDialog.Builder(activity)
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // log out here
                        (new LogoutAsyncTask(user_id, username, session, ((ChatApplication) activity.getApplication()).getURL() + "/logout")).execute();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        logoutConfirmAlertDialogBuilder.show();
    }

    public class LogoutAsyncTask extends AsyncTask<String, String, JSONObject> {
        private int user_id;
        private String username, session, url;

        public LogoutAsyncTask(int user_id, String username, String session, String url) {
            this.user_id = user_id;
            this.username = username;
            this.session = session;
            this.url = url;
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject inputJson = new JSONObject();
            try {
                inputJson.put("user_id", user_id);
                inputJson.put("username", username);
                inputJson.put("session", session);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            JSONParser jParser = new JSONParser();
            return jParser.getJSONFromUrl(url, inputJson);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            boolean logged_out = false;
            if(json != null) {
                try {
                    logged_out = json.getBoolean("logged out");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(logged_out) {
                Log.i("User", "User has successfully logged out.");
            } else {
                Log.i("User", "Failed to logout user from server. Removing shared preferences and restarting application anyway.");
            }

            // forget credentials if they are stored in SharedPreferences
            ((ChatApplication) activity.getApplication()).forgetCredentials();

            // Restart application
            Intent i = activity.getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( activity.getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);
        }
    }
}
