package com.example.saurabh.chat.network;

import android.os.AsyncTask;
import android.view.View;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.adapters.FriendsAdapter;
import com.example.saurabh.chat.fragments.FriendsListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FetchFriendsAsyncTask extends AsyncTask<String, String, ArrayList<FriendsAdapter.FriendsItem>> {

    FriendsListFragment friendsListFragment;
    String username, session;
    int user_id = -1;

    public FetchFriendsAsyncTask(FriendsListFragment friendsListFragment, String username, int user_id, String session) {
        this.friendsListFragment = friendsListFragment;
        this.username = username;
        this.user_id = user_id;
        this.session = session;
    }

    @Override
    protected ArrayList<FriendsAdapter.FriendsItem> doInBackground(String... params) {
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
        JSONObject outputJSON = jsonParser.getJSONFromUrl(((ChatApplication) friendsListFragment.getActivity().getApplication()).getURL() + "/fetchfriends", jsonObject);
        if(outputJSON == null) return null;
        ArrayList<FriendsAdapter.FriendsItem> friends;
        try {
            JSONArray friendsJSONArray = outputJSON.getJSONArray("friends");
            JSONObject friend;
            friends = new ArrayList<>(friendsJSONArray.length());

            for(int i = 0; i < friendsJSONArray.length(); i++) {
                friend = friendsJSONArray.getJSONObject(i);
                FriendsAdapter.FriendsItem item = new FriendsAdapter.FriendsItem(
                        friend.getString("username"),
                        friend.getString("date"));
                friends.add(item);
            }
            return friends;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final ArrayList<FriendsAdapter.FriendsItem> friends) {
        friendsListFragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                friendsListFragment.swipeContainer.setRefreshing(false);

                if(friends == null) {
                    friendsListFragment.swipeContainer.setVisibility(View.GONE);
                    friendsListFragment.statusLayout.setError("Unable to load friend list. Please try again later.");
                    return;
                }

                friendsListFragment.getAdapter().clear();
                friendsListFragment.getAdapter().addItems((ArrayList) friends);

                if(friendsListFragment.getAdapter().isEmpty()) {
                    friendsListFragment.swipeContainer.setVisibility(View.GONE);
                    friendsListFragment.statusLayout.setError("You haven't added any friends yet.");
                    return;
                }

                friendsListFragment.swipeContainer.setVisibility(View.VISIBLE);
                friendsListFragment.statusLayout.hide();
            }
        });
    }
}