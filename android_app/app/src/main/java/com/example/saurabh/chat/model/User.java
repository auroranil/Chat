package com.example.saurabh.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by saurabh on 7/01/16.
 */
public class User implements Parcelable {

    private static final Parcelable.Creator CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private int user_id = -1;
    private String username, session;

    public User(Parcel source) {
        user_id = source.readInt();
        username = source.readString();
        session = source.readString();
    }

    public User(int user_id, String username, String session) {
        this.user_id = user_id;
        this.username = username;
        this.session = session;
    }

    public int getUserID() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public boolean isLoggedIn() {
        return user_id > -1 && username != null && session != null;
    }

    public JSONObject serializeToJSON() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("user_id", user_id);
            jsonObject.put("username", username);
            jsonObject.put("session", session);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(user_id);
        dest.writeString(username);
        dest.writeString(session);
    }
}
