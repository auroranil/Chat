package com.example.saurabh.chat;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FetchRoomsAsyncTask extends AsyncTask<String, String, ArrayList<RoomAdapter.RoomItem>> {

    RoomsFragment roomsFragment;
    String username, session;
    int user_id = -1;

    public FetchRoomsAsyncTask(RoomsFragment roomsFragment, String username, int user_id, String session) {
        this.roomsFragment = roomsFragment;
        this.username = username;
        this.user_id = user_id;
        this.session = session;
    }

    @Override
    protected ArrayList<RoomAdapter.RoomItem> doInBackground(String... params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("user_id", user_id);
            jsonObject.put("session", session);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject outputJSON = jsonParser.getJSONFromUrl(((ChatApplication) roomsFragment.getActivity().getApplication()).getURL() + "/fetchrooms", jsonObject);
        if(outputJSON == null) return null;
        ArrayList<RoomAdapter.RoomItem> rooms;
        try {
            JSONArray roomsJSONArray = outputJSON.getJSONArray("rooms");
            JSONObject room;
            rooms = new ArrayList<>(roomsJSONArray.length());

            for(int i = 0; i < roomsJSONArray.length(); i++) {
                room = roomsJSONArray.getJSONObject(i);
                RoomAdapter.RoomItem item = new RoomAdapter.RoomItem(
                        room.getInt("room_id"),
                        room.getString("room_name"),
                        room.getString("username"),
                        room.getString("date"));
                rooms.add(item);
            }
            return rooms;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final ArrayList<RoomAdapter.RoomItem> rooms) {
        roomsFragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomsFragment.swipeContainer.setRefreshing(false);

                if(rooms == null) {
                    Toast.makeText(roomsFragment.getActivity(), "Unable to load room list. Please try again later.", Toast.LENGTH_LONG).show();
                    return;
                }

                roomsFragment.getAdapter().clear();
                roomsFragment.getAdapter().addItems((ArrayList) rooms);
            }
        });
    }
}