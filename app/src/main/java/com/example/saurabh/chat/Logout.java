package com.example.saurabh.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Logout {
    private static final int USERNAME = 0, SESSION = 1, URL = 2;

    Activity activity;

    public Logout(final Activity activity, final String username, final String session) {
        this.activity = activity;
        AlertDialog.Builder logoutConfirmAlertDialogBuilder = new AlertDialog.Builder(activity)
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // log out here
                        (new LogoutAsyncTask()).execute(username, session, WelcomeActivity.url + "/logout");
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
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject inputJson = new JSONObject();
            try {
                inputJson.put("username", args[USERNAME]);
                inputJson.put("session", args[SESSION]);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            JSONParser jParser = new JSONParser();
            return jParser.getJSONFromUrl(args[URL], inputJson);
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

            SharedPreferences sharedPreferences = activity.getSharedPreferences("user", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("username").remove("session").apply();

            // Restart application
            Intent i = activity.getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( activity.getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);
        }
    }
}
