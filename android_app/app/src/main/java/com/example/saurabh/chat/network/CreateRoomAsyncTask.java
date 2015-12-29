package com.example.saurabh.chat.network;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.activities.ChatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateRoomAsyncTask extends AsyncTask<String, String, JSONObject> {
    private Activity activity;
    private String username, session, room_name;
    private int user_id, room_id;

    public CreateRoomAsyncTask(Activity activity, int user_id, String username, String session, String room_name) {
        this.activity = activity;
        this.user_id = user_id;
        this.username = username;
        this.session = session;
        this.room_name = room_name;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", user_id);
            jsonObject.put("username", username);
            jsonObject.put("session", session);
            jsonObject.put("room_name", room_name);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        JSONParser jsonParser = new JSONParser();
        return jsonParser.getJSONFromUrl(((ChatApplication) activity.getApplication()).getURL() + "/createroom", jsonObject);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if(jsonObject == null) {
            Toast.makeText(activity, "Cannot create room '" + room_name + "'", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if(jsonObject.getBoolean("created") && jsonObject.has("room_id")) {
                room_id = jsonObject.getInt("room_id");
                Intent intent = new Intent(activity, ChatActivity.class);
                intent.putExtra("room_name", room_name);
                intent.putExtra("room_id", room_id);
                activity.startActivity(intent);
            } else {
                if(jsonObject.has("error")) {
                    Toast.makeText(activity, "Cannot create room '" + room_name + "': " + jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Cannot create room '" + room_name + "'", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Cannot create room '" + room_name + "'", Toast.LENGTH_SHORT).show();
        }
    }
}