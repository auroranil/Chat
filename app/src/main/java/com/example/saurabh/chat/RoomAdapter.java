package com.example.saurabh.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

class RoomAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final Context context;
    private final ArrayList<Object> mArrayList = new ArrayList<>();

    public RoomAdapter(Context context) {
        this.context = context;
    }

    public void clear() {
        mArrayList.clear();
        notifyDataSetChanged();
    }

    public void addItem(final Object item) {
        mArrayList.add(item);
        notifyDataSetChanged();
    }

    public void addItems(final ArrayList<Object> items) {
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
        LayoutInflater inflater = LayoutInflater.from(context);

        RoomViewHolder roomViewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.listview_rooms, parent, false);

            roomViewHolder = new RoomViewHolder();
            roomViewHolder.roomNameText = (TextView) convertView.findViewById(R.id.txt_room_name);
            roomViewHolder.usernameText = (TextView) convertView.findViewById(R.id.txt_username);
            roomViewHolder.timeAgoText = (TextView) convertView.findViewById(R.id.timeAgo);
            convertView.setTag(roomViewHolder);
        } else {
            roomViewHolder = (RoomViewHolder) convertView.getTag();
        }

        RoomItem roomItem = (RoomItem) getItem(position);
        roomViewHolder.roomNameText.setText(roomItem.getRoomName());
        roomViewHolder.usernameText.setText(roomItem.getUsername());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d;
        try {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = df.parse(roomItem.getDate());
            roomViewHolder.timeAgoText.setText(Utility.getTimeAgo(d.getTime()));
        } catch(ParseException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(context, "Item: " + position, Toast.LENGTH_SHORT).show();
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
        public TextView usernameText;
        public TextView timeAgoText;
    }
}
