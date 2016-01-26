package com.example.saurabh.chat.adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.R;
import com.example.saurabh.chat.fragments.FriendsListFragment;
import com.example.saurabh.chat.network.JSONParser;
import com.example.saurabh.chat.utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class FriendsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private static final int TYPE_FRIEND = 0, TYPE_FRIEND_REQUEST = 1;
    private int type;

    private final FriendsListFragment  friendsListFragment;
    private final Activity activity;
    private final ArrayList<Object> mArrayList = new ArrayList<>();

    public FriendsAdapter(FriendsListFragment friendsListFragment) {
        this.friendsListFragment = friendsListFragment;
        this.activity = friendsListFragment.getActivity();
    }

    public void clear() {
        mArrayList.clear();
        notifyDataSetChanged();
    }

    public void addItem(final Object item) {
        if(item == null) return;
        mArrayList.add(item);
        notifyDataSetChanged();
    }

    public void addItems(final ArrayList<Object> items) {
        if(items == null) return;
        mArrayList.addAll(items);
        notifyDataSetChanged();
    }

    public void prependItems(final ArrayList<Object> items) {
        if(getCount() > 0) {
            mArrayList.addAll(0, items);
        } else {
            addItems(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(mArrayList.get(position) instanceof FriendItem) {
            type = TYPE_FRIEND;
        } else if(mArrayList.get(position) instanceof FriendRequestItem) {
            type = TYPE_FRIEND_REQUEST;
        }

        return type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);

        switch (getItemViewType(position)) {
            case TYPE_FRIEND:
                final FriendViewHolder friendViewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_friend, parent, false);

                    friendViewHolder = new FriendViewHolder();
                    friendViewHolder.usernameText = (TextView) convertView.findViewById(R.id.txt_username);
                    friendViewHolder.becameFriendsText = (TextView) convertView.findViewById(R.id.txt_became_friends);
                    convertView.setTag(friendViewHolder);
                } else {
                    friendViewHolder = (FriendViewHolder) convertView.getTag();
                }

                final FriendItem friendItem = (FriendItem) getItem(position);
                friendViewHolder.usernameText.setText(friendItem.getUsername());
                final Date createdDate = Utility.parseDateAsUTC(friendItem.getDate());
                if (createdDate != null) {
                    friendViewHolder.becameFriendsText.setText(activity.getResources().getString(R.string.became_friends, Utility.getTimeAgo(createdDate)));
                }
                break;
            case TYPE_FRIEND_REQUEST:
                final FriendRequestViewHolder friendRequestViewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_friend_request, parent, false);

                    friendRequestViewHolder = new FriendRequestViewHolder();
                    friendRequestViewHolder.usernameText = (TextView) convertView.findViewById(R.id.txt_username);
                    friendRequestViewHolder.acceptBtn = (Button) convertView.findViewById(R.id.btn_accept);
                    friendRequestViewHolder.ignoreBtn = (Button) convertView.findViewById(R.id.btn_ignore);
                    convertView.setTag(friendRequestViewHolder);
                } else {
                    friendRequestViewHolder = (FriendRequestViewHolder) convertView.getTag();
                }

                final FriendRequestItem friendRequestItem = (FriendRequestItem) getItem(position);

                friendRequestViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (new FriendAsyncTask(friendRequestItem.getUserID())).execute();
                        friendRequestViewHolder.acceptBtn.setEnabled(false);
                        friendRequestViewHolder.acceptBtn.setText(activity.getResources().getString(R.string.sending_friend_req));
                        friendRequestViewHolder.ignoreBtn.setEnabled(false);
                    }
                });

                friendRequestViewHolder.usernameText.setText(friendRequestItem.getUsername());
                break;
            default:
                break;
        }

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(activity, "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    public static class FriendItem {
        private final String username, date;
        private final int user_id;

        public FriendItem(String username, int user_id, String date) {
            this.username = username;
            this.date = date;
            this.user_id = user_id;
        }

        public String getUsername() {
            return username;
        }
        public String getDate() {
            return date;

        }
        public int getUserID() { return user_id; }
    }

    public static class FriendRequestItem {
        private final String username;
        private final int user_id;

        public FriendRequestItem(String username, int user_id) {
            this.username = username;
            this.user_id = user_id;
        }

        public String getUsername() {
            return username;
        }
        public int getUserID() { return user_id; }
    }

    public static class FriendViewHolder {
        public TextView usernameText;
        public TextView becameFriendsText;
    }

    public static class FriendRequestViewHolder {
        public TextView usernameText;
        public Button acceptBtn;
        public Button ignoreBtn;
    }

    private class FriendAsyncTask extends AsyncTask<String, String, JSONObject> {
        int look_up_user_id;

        public FriendAsyncTask(int look_up_user_id) {
            this.look_up_user_id = look_up_user_id;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                JSONObject jsonObject = ((ChatApplication) activity.getApplication()).getUser().serializeToJSON();
                jsonObject.put("friend_user_id", look_up_user_id);
                return new JSONParser().getJSONFromUrl(((ChatApplication) activity.getApplication()).getURL() + "/friend", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject == null) {
                Toast.makeText(activity.getBaseContext(), "Unable to perform action.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                if(jsonObject.getBoolean("success")) {
                    friendsListFragment.refresh();
                } else {
                    Toast.makeText(activity.getBaseContext(), "Unable to perform action.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(activity.getBaseContext(), "Unable to perform action.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
