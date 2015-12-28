package com.example.saurabh.chat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ChatApplication extends Application {
    private static final String TAG = "ChatApplication";
    private String url;
    public int test = 0;

    private SharedPreferences sharedPreferences;

    private String username, session;
    private int user_id = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences =
                getBaseContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        url = sharedPreferences.getString("url", null);
        username = sharedPreferences.getString("username", null);
        user_id = sharedPreferences.getInt("user_id", -1);
        session = sharedPreferences.getString("session", null);

    }

    public String getUsername() {
        return username;
    }

    public int getUserID() {
        return user_id;
    }

    public String getSession() {
        return session;
    }

    public String getURL() {
        return url;
    }

    public void setCredentials(String url, int user_id, String username, String session) {
        this.url = url;
        this.user_id = user_id;
        this.username = username;
        this.session = session;
    }

    public boolean isLoggedIn() {
        return url != null && user_id > -1 && username != null && session != null;
    }

    /**
     * Must call setCredentials methods with non-null parameters before using this method
     */
    public void rememberCredentials() {
        if(url == null || username == null || user_id < 0 || session == null) {
            Log.e(TAG, "Error: Must set credentials first");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("url", url)
                .putInt("user_id", user_id)
                .putString("username", username)
                .putString("session", session).apply();
    }

    public void forgetCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username").clear().apply();

        url = null;
        user_id = -1;
        username = null;
        session = null;
    }
}
