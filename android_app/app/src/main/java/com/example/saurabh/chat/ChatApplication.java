package com.example.saurabh.chat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.saurabh.chat.model.User;

public class ChatApplication extends Application {
    private static final String TAG = "ChatApplication";
    private String url;

    private SharedPreferences sharedPreferences;

    private User user;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences =
                getBaseContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        url = sharedPreferences.getString("url", null);
        user = new User(
                sharedPreferences.getInt("user_id", -1),
                sharedPreferences.getString("username", null),
                sharedPreferences.getString("session", null)
        );
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public User getUser() {
        return  user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return url != null && user.isLoggedIn();
    }

    /**
     * Must call setCredentials methods with non-null parameters before using this method
     */
    public void rememberCredentials() {
        if(!isLoggedIn()) {
            Log.e(TAG, "Error: Must set credentials first");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("url", url)
                .putInt("user_id", user.getUserID())
                .putString("username", user.getUsername())
                .putString("session", user.getSession()).apply();
    }

    public void forgetCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();

        url = null;
        user = null;
    }
}
