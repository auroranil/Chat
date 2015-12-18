package com.example.saurabh.chat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by saurabh on 18/12/15.
 */
public class ChatApplication extends Application {
    private static final String TAG = "ChatApplication";
    public final String url = "http://10.0.0.43:5000";

    private SharedPreferences sharedPreferences;

    private String username, session;
    private int user_id = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences =
                getBaseContext().getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    public String getUsername() {
        // fetch username from sharedPreferences if it is stored and username string is null
        if(username == null && sharedPreferences.contains("username")) {
            username = sharedPreferences.getString("username", null);
        }

        return username;
    }

    public int getUserID() {
        // fetch user id from sharedPreferences if it is stored and user_id is equal to -1
        if(user_id == -1 && sharedPreferences.contains("user_id")) {
            user_id = sharedPreferences.getInt("user_id", -1);
        }

        return user_id;
    }

    public String getSession() {
        // fetch session from sharedPreferences if it is stored and session string is null
        if(session == null && sharedPreferences.contains("session")) {
            session = sharedPreferences.getString("session", null);
        }

        return session;
    }


    public void setCredentials(int user_id, String username, String session) {
        this.user_id = user_id;
        this.username = username;
        this.session = session;
    }

    public boolean isLoggedIn() {
        return user_id > -1 && username != null && session != null || sharedPreferences.contains("user_id") && sharedPreferences.contains("username") && sharedPreferences.contains("session");
    }

    /**
     * Must call setCredentials method with non-null parameters before using this method
     */
    public void rememberCredentials() {
        if(username == null || user_id < 0 || session == null) {
            Log.e(TAG, "Error: Must set credentials first");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", user_id)
                .putString("username", username)
                .putString("session", session).apply();
    }

    public void forgetCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username").remove("user_id").remove("session").apply();
    }
}
