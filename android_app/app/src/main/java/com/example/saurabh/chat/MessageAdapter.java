package com.example.saurabh.chat;

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

import java.util.ArrayList;

class MessageAdapter extends BaseAdapter {
    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_BROADCAST = 1;

    private static final int MSG_MENU_COPY_TEXT = 0;
    private static final int MSG_MENU_VIEW_DETAILS = 1;
    private static final int MSG_MENU_VIEW_PROFILE = 2;

    private final String username;

    private final Context context;
    private final ArrayList<Object> mArrayList = new ArrayList<>();

    private int type = TYPE_MESSAGE;

    public MessageAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
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
                final MessageViewHolder messageViewHolder;
                if(convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_messages, parent, false);

                    messageViewHolder = new MessageViewHolder();
                    messageViewHolder.usernameText = (TextView) convertView.findViewById(R.id.username_display);
                    messageViewHolder.messageText = (TextView) convertView.findViewById(R.id.message_display);
                    messageViewHolder.message = (LinearLayout) convertView.findViewById(R.id.message);
                    messageViewHolder.bg = messageViewHolder.messageText.getBackground();

                    messageViewHolder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder messageChoice = new AlertDialog.Builder(context);

                            messageChoice
                                    .setTitle("Message options")
                                    .setItems(R.array.message_dialog_choice_list, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch(which) {
                                                case MSG_MENU_COPY_TEXT:
                                                    ClipboardManager clipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("chat message", messageViewHolder.messageText.getText().toString());
                                                    clipMan.setPrimaryClip(clip);

                                                    Toast.makeText(context, "Message copied to clipboard.", Toast.LENGTH_LONG).show();
                                                    break;
                                                case MSG_MENU_VIEW_DETAILS:
                                                    break;
                                                case MSG_MENU_VIEW_PROFILE:
                                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                                    intent.putExtra("username", messageViewHolder.usernameText.getText().toString());
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

                    convertView.setTag(messageViewHolder);
                } else {
                    messageViewHolder = (MessageViewHolder) convertView.getTag();
                }

                MessageItem msg_item = (MessageItem) getItem(position);
                messageViewHolder.usernameText.setText(msg_item.getUsername());
                messageViewHolder.messageText.setText(msg_item.getMessage());
                // add all links if message contains them
                Linkify.addLinks(messageViewHolder.messageText, Linkify.ALL);

                if(username.equals(msg_item.getUsername())) {
                    messageViewHolder.message.setGravity(Gravity.END);
                    if (messageViewHolder.bg instanceof ShapeDrawable) {
                        ((ShapeDrawable) messageViewHolder.bg).getPaint().setColor(Color.parseColor("#FFF8DC"));
                    } else if (messageViewHolder.bg instanceof GradientDrawable) {
                        ((GradientDrawable) messageViewHolder.bg).setColor(Color.parseColor("#FFF8DC"));
                    }
                } else {
                    messageViewHolder.message.setGravity(Gravity.START);
                    if (messageViewHolder.bg instanceof ShapeDrawable) {
                        ((ShapeDrawable) messageViewHolder.bg).getPaint().setColor(Color.parseColor("#FFFFFF"));
                    } else if (messageViewHolder.bg instanceof GradientDrawable) {
                        ((GradientDrawable) messageViewHolder.bg).setColor(Color.parseColor("#FFFFFF"));
                    }
                }

                messageViewHolder.usernameText.setVisibility(View.VISIBLE);

                if(position > 0) {
                    Object prev_msg_item = getItem(position - 1);
                    if (prev_msg_item instanceof MessageItem) {
                        if (msg_item.getUsername().equals(((MessageItem) prev_msg_item).getUsername())) {
                            messageViewHolder.usernameText.setVisibility(View.GONE);
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
        private final int user_id;
        private final String username;
        private final String message;
        private final String datetime_utc;

        public MessageItem(int user_id, String username, String message, String datetime_utc) {
            this.user_id = user_id;
            this.username = username;
            this.message = message;
            this.datetime_utc = datetime_utc;
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
        public TextView usernameText;
        public TextView messageText;
        public Drawable bg;
    }

    public static class BroadcastViewHolder {
        public TextView broadcastMsg;
    }
}
