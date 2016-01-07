package com.example.saurabh.chat.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saurabh.chat.R;
import com.example.saurabh.chat.utilities.Utility;

import java.util.ArrayList;
import java.util.Date;

public class FriendsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private static final int TYPE_FRIEND = 0, TYPE_FRIEND_REQUEST = 1;
    private int type;

    private final Activity activity;
    private final ArrayList<Object> mArrayList = new ArrayList<>();

    public FriendsAdapter(Activity activity) {
        this.activity = activity;
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

                FriendItem friendItem = (FriendItem) getItem(position);
                friendViewHolder.usernameText.setText(friendItem.getUsername());
                final Date createdDate = Utility.parseDateAsUTC(friendItem.getDate());
                if (createdDate != null) {
                    friendViewHolder.becameFriendsText.setText(activity.getResources().getString(R.string.became_friends, Utility.getTimeAgo(createdDate.getTime())));
                }
                break;
            case TYPE_FRIEND_REQUEST:
                final FriendRequestViewHolder friendRequestViewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_friend_request, parent, false);

                    friendRequestViewHolder = new FriendRequestViewHolder();
                    friendRequestViewHolder.usernameText = (TextView) convertView.findViewById(R.id.txt_username);
                    convertView.setTag(friendRequestViewHolder);
                } else {
                    friendRequestViewHolder = (FriendRequestViewHolder) convertView.getTag();
                }

                FriendRequestItem friendRequestItem = (FriendRequestItem) getItem(position);
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

        public FriendItem(String username, String date) {
            this.username = username;
            this.date = date;
        }

        public String getUsername() {
            return username;
        }
        public String getDate() {
            return date;
        }
    }

    public static class FriendRequestItem {
        private final String username;

        public FriendRequestItem(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
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
}
