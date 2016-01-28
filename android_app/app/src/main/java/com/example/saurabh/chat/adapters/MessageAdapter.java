package com.example.saurabh.chat.adapters;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saurabh.chat.R;
import com.example.saurabh.chat.activities.UserProfileActivity;
import com.example.saurabh.chat.utilities.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    private static final String TAG = "adapters/MessageAdapter";

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_BROADCAST = 1;

    private static final int MSG_MENU_COPY_TEXT = 0;
    private static final int MSG_MENU_VIEW_DETAILS = 1;
    private static final int MSG_MENU_VIEW_PROFILE = 2;

    private final String username;

    private final Context context;
    private final ArrayList<Object> mArrayList = new ArrayList<>();

    LinearLayout.LayoutParams detailsLinearLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );

    private int type = TYPE_MESSAGE;

    public MessageAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
    }

    public int addItem(final Object item) {
        mArrayList.add(item);
        notifyDataSetChanged();
        return mArrayList.size() - 1;
    }

    public int moveItemToEndOfList(int index) {
        mArrayList.add(mArrayList.remove(index));
        return mArrayList.size() - 1;
    }

    public void addItems(final ArrayList<Object> items) {
        mArrayList.addAll(items);
        notifyDataSetChanged();
    }

    // TODO: fix for broadcastItem
    public int getFirstID() {
        if(getCount() == 0) return -1;
        return ((MessageItem) mArrayList.get(0)).getID();
    }

    // TODO: fix for broadcastItem
    public int getLastID() {
        if(getCount() == 0) return -1;
        return ((MessageItem) mArrayList.get(getCount() - 1)).getID();
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
    public int getItemViewType(int position) {
        if(mArrayList.get(position) instanceof MessageItem) {
            type = TYPE_MESSAGE;
        } else if(mArrayList.get(position) instanceof BroadcastItem) {
            type = TYPE_BROADCAST;
        }

        return type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (getItemViewType(position)) {
            case TYPE_BROADCAST:
                BroadcastViewHolder broadcastViewHolder;
                if(convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_broadcast, parent, false);

                    broadcastViewHolder = new BroadcastViewHolder();
                    broadcastViewHolder.broadcastMsg = (TextView) convertView.findViewById(R.id.broadcast_msg);

                    convertView.setTag(broadcastViewHolder);
                } else {
                    broadcastViewHolder = (BroadcastViewHolder) convertView.getTag();
                }

                BroadcastItem broadcastItem = (BroadcastItem) getItem(position);
                broadcastViewHolder.broadcastMsg.setText(broadcastItem.getMessage());
                break;
            case TYPE_MESSAGE:
                final MessageItem msg_item = (MessageItem) getItem(position);
                final MessageViewHolder messageViewHolder;
                if(convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_messages, parent, false);

                    messageViewHolder = new MessageViewHolder();
                    messageViewHolder.detailsText = (TextView) convertView.findViewById(R.id.details_display);
                    messageViewHolder.messageText = (TextView) convertView.findViewById(R.id.message_display);
                    messageViewHolder.message = (LinearLayout) convertView.findViewById(R.id.message);
                    messageViewHolder.bg = messageViewHolder.messageText.getBackground();

                    convertView.setTag(messageViewHolder);
                } else {
                    messageViewHolder = (MessageViewHolder) convertView.getTag();
                }

                // need to reset listener (expensive but necessary to fetch correct user ID for user profile)
                messageViewHolder.message.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final AlertDialog.Builder messageChoice = new AlertDialog.Builder(context);

                        messageChoice
                                .setTitle("Message options")
                                .setItems(R.array.message_dialog_choice_list, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case MSG_MENU_COPY_TEXT:
                                                ClipboardManager clipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("chat message", messageViewHolder.messageText.getText().toString());
                                                clipMan.setPrimaryClip(clip);

                                                Toast.makeText(context, "Message copied to clipboard.", Toast.LENGTH_LONG).show();
                                                break;
                                            case MSG_MENU_VIEW_DETAILS:
                                                AlertDialog.Builder detailsDialog = new AlertDialog.Builder(context);
                                                StringBuilder messageStr = new StringBuilder();

                                                messageStr.append("Type: Message");
                                                messageStr.append("\n");
                                                messageStr.append("From: ");
                                                messageStr.append(msg_item.getUsername());
                                                messageStr.append("\n");
                                                messageStr.append("Sent: ");
                                                if(msg_item.on_server) {
                                                    messageStr.append(
                                                            new SimpleDateFormat("d MMMM yyyy h:mm a")
                                                                    .format(Utility.parseDateAsUTC(msg_item.getDateTimeUTC()))
                                                                    .replace("AM", "am")
                                                                    .replace("PM", "pm")
                                                    );
                                                } else {
                                                    messageStr.append("Sending...");
                                                }

                                                detailsDialog
                                                        .setTitle("Message details")
                                                        .setMessage(messageStr.toString())
                                                        .show();
                                                break;
                                            case MSG_MENU_VIEW_PROFILE:
                                                Intent intent = new Intent(context, UserProfileActivity.class);
                                                intent.putExtra("user_id", msg_item.user_id);
                                                context.startActivity(intent);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });

                        messageChoice.show();
                        return true;
                    }
                });

                messageViewHolder.messageText.setText(msg_item.getMessage());
                // add all links if message contains them
                Linkify.addLinks(messageViewHolder.messageText, Linkify.ALL);

                if(username.equals(msg_item.getUsername())) {
                    messageViewHolder.message.setGravity(Gravity.END);
                    detailsLinearLayoutParams.setMargins(0, 0, 32, 32);
                    messageViewHolder.detailsText.setLayoutParams(detailsLinearLayoutParams);
                    if (messageViewHolder.bg instanceof ShapeDrawable) {
                        ((ShapeDrawable) messageViewHolder.bg).getPaint().setColor(Color.parseColor("#FFDDDDDD"));
                    } else if (messageViewHolder.bg instanceof GradientDrawable) {
                        ((GradientDrawable) messageViewHolder.bg).setColor(Color.parseColor("#FFDDDDDD"));
                    }
                } else {
                    messageViewHolder.message.setGravity(Gravity.START);
                    detailsLinearLayoutParams.setMargins(32, 0, 0, 32);
                    messageViewHolder.detailsText.setLayoutParams(detailsLinearLayoutParams);
                    if (messageViewHolder.bg instanceof ShapeDrawable) {
                        ((ShapeDrawable) messageViewHolder.bg).getPaint().setColor(Color.parseColor("#FFFFFFFF"));
                    } else if (messageViewHolder.bg instanceof GradientDrawable) {
                        ((GradientDrawable) messageViewHolder.bg).setColor(Color.parseColor("#FFFFFFFF"));
                    }
                }

                StringBuilder details = new StringBuilder();

                if(!username.equals(msg_item.getUsername())) {
                    details.append(msg_item.getUsername());
                    details.append(" - ");
                }

                if(msg_item.on_server) {
                    details.append(Utility.getAbbreviatedDateTime(Utility.parseDateAsUTC(msg_item.getDateTimeUTC())));
                }

                messageViewHolder.detailsText.setText(details.toString());

                messageViewHolder.detailsText.setVisibility(View.VISIBLE);

                if(!msg_item.on_server) {
                    messageViewHolder.detailsText.setText("Sending");
                }
                else if(position + 1 < mArrayList.size()) {
                    Object next_msg_item = getItem(position + 1);
                    if (next_msg_item instanceof MessageItem) {
                        if (msg_item.getUsername().equals(((MessageItem) next_msg_item).getUsername())) {
                            messageViewHolder.detailsText.setVisibility(View.GONE);
                        }
                    }
                }

                break;
            default:
                break;
        }

        return convertView;
    }

    public static class MessageItem {
        private int id;
        private final int user_id;
        private final String username;
        private final String message;
        private String datetime_utc;
        private boolean on_server;

        public MessageItem(int id, int user_id, String username, String message, String datetime_utc) {
            this.on_server = true;
            this.id = id;
            this.user_id = user_id;
            this.username = username;
            this.message = message;
            this.datetime_utc = datetime_utc;
        }

        public MessageItem(int user_id, String username, String message) {
            this.on_server = false;
            this.user_id = user_id;
            this.username = username;
            this.message = message;
        }

        public int getID() {
            return id;
        }

        public int getUserID() {
            return user_id;
        }

        public String getUsername() {
            return username;
        }

        public String getMessage() {
            return message;
        }

        public String getDateTimeUTC() {
            return datetime_utc;
        }

        public void savedToServer(int id, String datetime_utc) {
            this.on_server = true;
            this.id = id;
            this.datetime_utc = datetime_utc;
        }

        @Override
        public String toString() {
            return username + ": " + message;
        }
    }

    public static class BroadcastItem {
        private final String message;

        public BroadcastItem(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class MessageViewHolder {
        public LinearLayout message;
        public TextView detailsText;
        public TextView messageText;
        public Drawable bg;
    }

    public static class BroadcastViewHolder {
        public TextView broadcastMsg;
    }
}
