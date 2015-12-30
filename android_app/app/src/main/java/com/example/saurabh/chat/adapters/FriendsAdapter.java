package com.example.saurabh.chat.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saurabh.chat.R;
import com.example.saurabh.chat.utilities.Utility;

import java.util.ArrayList;
import java.util.Date;

public class FriendsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

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
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);

        final FriendsViewHolder friendsViewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.listview_friend, parent, false);

            friendsViewHolder = new FriendsViewHolder();
            friendsViewHolder.usernameText = (TextView) convertView.findViewById(R.id.txt_username);
            friendsViewHolder.becameFriendsText = (TextView) convertView.findViewById(R.id.txt_became_friends);
            convertView.setTag(friendsViewHolder);
        } else {
            friendsViewHolder = (FriendsViewHolder) convertView.getTag();
        }

        FriendsItem friendsItem = (FriendsItem) getItem(position);
        friendsViewHolder.usernameText.setText(friendsItem.getUsername());
        final Date createdDate = Utility.parseDateAsUTC(friendsItem.getDate());
        if(createdDate != null) {
            friendsViewHolder.becameFriendsText.setText(activity.getResources().getString(R.string.became_friends, Utility.getTimeAgo(createdDate.getTime())));
        }

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(activity, "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    public static class FriendsItem {
        private final String username, date;

        public FriendsItem(String username, String date) {
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

    public static class FriendsViewHolder {
        public TextView usernameText;
        public TextView becameFriendsText;
    }
}
