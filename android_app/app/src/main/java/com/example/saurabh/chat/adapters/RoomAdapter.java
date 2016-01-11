package com.example.saurabh.chat.adapters;

import android.app.Activity;
import android.content.res.Resources;
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

public class RoomAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final Activity activity;
    private final Resources res;
    private final ArrayList<Object> mArrayList = new ArrayList<>();

    public RoomAdapter(Activity activity) {
        this.activity = activity;
        res = activity.getResources();
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

        final RoomViewHolder roomViewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.listview_rooms, parent, false);

            roomViewHolder = new RoomViewHolder();
            roomViewHolder.roomNameText = (TextView) convertView.findViewById(R.id.txt_room_name);
            roomViewHolder.createdByUserText = (TextView) convertView.findViewById(R.id.txt_created_by_user);
            convertView.setTag(roomViewHolder);
        } else {
            roomViewHolder = (RoomViewHolder) convertView.getTag();
        }

        RoomItem roomItem = (RoomItem) getItem(position);
        roomViewHolder.roomNameText.setText(roomItem.getRoomName());

        final Date createdDate = Utility.parseDateAsUTC(roomItem.getDate());
        if(createdDate != null) {
            roomViewHolder.createdByUserText.setText(res.getString(R.string.created_by_user, roomItem.getUsername() + " " + Utility.getTimeAgo(createdDate)));
        }

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(activity, "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    public static class RoomItem {
        private final String room_name, username, date;
        private final int room_id;

        public RoomItem(int room_id, String room_name, String username, String date) {
            this.room_id = room_id;
            this.room_name = room_name;
            this.username = username;
            this.date = date;
        }

        public int getRoomID() {
            return room_id;
        }

        public String getRoomName() {
            return room_name;
        }

        public String getUsername() {
            return username;
        }

        public String getDate() {
            return date;
        }
    }

    public static class RoomViewHolder {
        public TextView roomNameText;
        public TextView createdByUserText;
    }
}
