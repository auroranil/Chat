package com.example.saurabh.chat.network;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.activities.MenuActivity;
import com.example.saurabh.chat.adapters.RoomAdapter;
import com.example.saurabh.chat.fragments.RoomsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FetchRoomsAsyncTask extends AsyncTask<String, String, JSONObject> {
    private static final String TAG = "network/FetchRooms~Task";

    RoomsFragment roomsFragment;
    String username, session;
    int user_id = -1;

    public FetchRoomsAsyncTask(RoomsFragment roomsFragment, String username, int user_id, String session) {
        this.roomsFragment = roomsFragment;
        this.username = username;
        this.user_id = user_id;
        this.session = session;
    }

    public static ArrayList<RoomAdapter.RoomItem> deserialize(JSONObject jsonObject) {
        if(jsonObject == null) return null;
        ArrayList<RoomAdapter.RoomItem> rooms;
        try {
            JSONArray roomsJSONArray = jsonObject.getJSONArray("rooms");
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
    protected JSONObject doInBackground(String... params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("user_id", user_id);
            jsonObject.put("session", session);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return (new JSONParser()).getJSONFromUrl(((ChatApplication) roomsFragment.getActivity().getApplication()).getURL() + "/fetchrooms", jsonObject);
    }

    @Override
    protected void onPostExecute(final JSONObject jsonObject) {
        if(jsonObject == null) {
            ((MenuActivity) roomsFragment.getActivity()).unableToConnectSnackBar();

            if(((ChatApplication) roomsFragment.getActivity().getApplication()).isRemembered()) {
                boolean error = false;

                try {
                    FileInputStream fis = roomsFragment.getActivity().openFileInput("rooms.json");
                    String buffer = "";
                    int c;

                    while ((c = fis.read()) != -1) {
                        if(c == 0) continue;
                        buffer += Character.toString((char) c);
                    }

                    fis.close();

                    if(!buffer.isEmpty()) {
                        JSONObject cachedJson = new JSONObject(buffer);
                        roomsFragment.getAdapter().clear();
                        roomsFragment.getAdapter().addItems((ArrayList) FetchRoomsAsyncTask.deserialize(cachedJson));
                        roomsFragment.swipeContainer.setRefreshing(false);
                        roomsFragment.swipeContainer.setVisibility(View.VISIBLE);
                        roomsFragment.statusLayout.hide();
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    error = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    error = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    error = true;
                }

                if(error) {
                    roomsFragment.swipeContainer.setVisibility(View.GONE);
                    roomsFragment.statusLayout.setError("Unable to load room list. Please try again later.");
                    return;
                }
            }
        }

        if(((ChatApplication) roomsFragment.getActivity().getApplication()).isRemembered()) {
            try {
                FileOutputStream fos = roomsFragment.getActivity().openFileOutput("rooms.json", Context.MODE_PRIVATE);
                fos.write(jsonObject.toString().getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final ArrayList<RoomAdapter.RoomItem> rooms = deserialize(jsonObject);
        roomsFragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomsFragment.swipeContainer.setRefreshing(false);

                if(rooms == null) {
                    roomsFragment.swipeContainer.setVisibility(View.GONE);
                    roomsFragment.statusLayout.setError("Unable to load room list. Please try again later.");
                    return;
                }

                roomsFragment.getAdapter().clear();
                roomsFragment.getAdapter().addItems((ArrayList) rooms);

                roomsFragment.swipeContainer.setVisibility(View.VISIBLE);
                roomsFragment.statusLayout.hide();
            }
        });
    }
}