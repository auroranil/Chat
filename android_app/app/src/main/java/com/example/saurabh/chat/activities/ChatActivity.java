package com.example.saurabh.chat.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.R;
import com.example.saurabh.chat.adapters.MessageAdapter;
import com.example.saurabh.chat.model.User;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "activities/ChatActivity";

    private static final int ROOM = 0, FRIEND = 1;
    private ListView listViewMessages;
    private MessageAdapter adapter;
    private EditText txtMessage;
    private LinearLayout footerView;
    private TextView usersTypingTextView;
    private TextView isTypingTextView;
    private boolean typing = false;
    private boolean first_history = true;
    private boolean history_lock = false;

    private final ArrayList<String> usersTyping = new ArrayList<>();
    private Resources res;

    private JSONObject info;
    private Socket mSocket;

    // This array keeps track of which list items have not been
    // sent to the server.
    private ArrayList<Integer> not_on_server_indices = new ArrayList<>();

    int user_id;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        res = getResources();

        Intent intent = getIntent();
        ChatApplication chatApplication = (ChatApplication) this.getApplication();
        User user = chatApplication.getUser();
        user_id = user.getUserID();
        username = user.getUsername();
        String session = user.getSession();
        String room_name = intent.getStringExtra("room_name");
        // TODO: deal with default value
        int room_id = intent.getIntExtra("room_id", -1);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Room: " + room_name);
        }

        try {
            mSocket = IO.socket(((ChatApplication) getApplication()).getURL());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.connect();

        info = new JSONObject();
        try {
            info.put("user_id", user_id);
            info.put("username", username);
            info.put("session", session);
            info.put("room_name", room_name);
            info.put("room_id", room_id);
            info.put("type", ROOM);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.on("received message", onMessageReceive);
        mSocket.on("broadcast", onBroadcast);
        mSocket.on("history", onHistory);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);

        mSocket.emit("join", info);
        mSocket.emit("fetch messages", info);
        history_lock = true;

        listViewMessages = (ListView) findViewById(R.id.listView_messages);
        txtMessage = (EditText) findViewById(R.id.txt_message);

        footerView = (LinearLayout) findViewById(R.id.layout_typing);
        footerView.setVisibility(View.GONE);

        usersTypingTextView = (TextView) footerView.findViewById(R.id.users_typing);
        isTypingTextView = (TextView) footerView.findViewById(R.id.is_typing);

        adapter = new MessageAdapter(ChatActivity.this, username);

        listViewMessages.setAdapter(adapter);

        listViewMessages.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (adapter.getCount() == 0) {
                    return;
                }

                if (firstVisibleItem == 0) {
                    // check if we reached the top or bottom of the list
                    View v = listViewMessages.getChildAt(0);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset < 2) {
                        // reached the top:
                        Log.d("ChatActivity", "history_lock=" + history_lock);
                        if (!history_lock) {
                            try {
                                JSONObject json = new JSONObject(info.toString());
                                if(adapter.getCount() > 0) {
                                    json.put("before_msg_id", adapter.getFirstID());
                                }
                                mSocket.emit("fetch messages", json);
                                history_lock = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        final Button btnSend = (Button) findViewById(R.id.btn_send);

        txtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String message = txtMessage.getText().toString();
                btnSend.setEnabled(!message.isEmpty());

                if (!message.isEmpty() && !typing) {
                    typing = true;
                    mSocket.emit("typing", info);
                } else if (message.isEmpty() && typing) {
                    typing = false;
                    mSocket.emit("stop typing", info);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msg = txtMessage.getText().toString();
                txtMessage.setText("");

                // Don't send message if string is empty
                if (!msg.isEmpty()) {
                    new SendMessageTask(msg).execute();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mSocket.emit("leave", info);
        mSocket.disconnect();
        mSocket.off("received message", onMessageReceive);
        mSocket.off("broadcast", onBroadcast);
        mSocket.off("history", onHistory);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);

        super.onDestroy();
    }

    private final Emitter.Listener onMessageReceive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("message received", "a");
            JSONObject json;
            final int msg_user_id, message_id;
            final String msg_username, message_contents, datetimeutc;
            try {
                json = (JSONObject) args[0];

                msg_user_id = json.getInt("user_id");
                message_id = json.getInt("message_id");
                msg_username = json.getString("username");
                message_contents = json.getString("message");
                datetimeutc = json.getString("datetimeutc");

                if(user_id == msg_user_id && not_on_server_indices.size() > 0) {
                    /** TODO: remove assumption that messages are received in order
                      * Proper way is to sort messages by their IDs in ascending order
                      **/

                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MessageAdapter.MessageItem msg_item = (MessageAdapter.MessageItem) adapter.getItem(not_on_server_indices.get(0));
                            msg_item.savedToServer(message_id, datetimeutc);

                            // In case there are messages that were sent to server before this one,
                            // move it to the end of the list.
                            // adapter.moveItemToEndOfList(not_on_server_indices.get(0));

                            listViewMessages.setAdapter(listViewMessages.getAdapter());
                            not_on_server_indices.remove(0);
                        }
                    });
                } else {
                    final MessageAdapter.MessageItem msgItem = new MessageAdapter.MessageItem(message_id, user_id, msg_username, message_contents, datetimeutc);

                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addItem(msgItem);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private final Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            usersTyping.add((String) args[0]);

            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String usersTypingStr = usersTyping.toString();
                    usersTypingTextView.setText(usersTypingStr.substring(1, usersTypingStr.length() - 1));

                    if (usersTyping.size() == 1) {
                        // show view
                        footerView.setVisibility(View.VISIBLE);
                        isTypingTextView.setText(res.getString(R.string.is_typing));
                    }

                    if (usersTyping.size() == 2) {
                        isTypingTextView.setText(res.getString(R.string.are_typing));
                    }
                }
            });
        }
    };

    private final Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            usersTyping.remove((String) args[0]);

            if (usersTyping.isEmpty()) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String usersTypingStr = usersTyping.toString();
                        usersTypingTextView.setText(usersTypingStr.substring(1, usersTypingStr.length() - 1));

                        if (usersTyping.size() == 1) {
                            isTypingTextView.setText(res.getString(R.string.is_typing));
                        }

                        if (usersTyping.isEmpty()) {
                            // hide view
                            footerView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    };

    private final Emitter.Listener onBroadcast = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final MessageAdapter.BroadcastItem broadcastItem = new MessageAdapter.BroadcastItem((String) args[0]);

            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.addItem(broadcastItem);
                }
            });
        }
    };

    private final Emitter.Listener onHistory = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject json;
            JSONArray arr;

            Log.i("ChatActivity", "receiving message history");

            final ArrayList<Object> items = new ArrayList<>();

            try {
                json = (JSONObject) args[0];
                arr = json.getJSONArray("history");
                Log.d("arr size", Integer.toString(arr.length()));
                JSONObject jsonObject;

                for (int i = 0; i < arr.length(); i++) {
                    Log.i("Index", Integer.toString(i));
                    jsonObject = arr.getJSONObject(i);

                    items.add(
                            new MessageAdapter.MessageItem(
                                    jsonObject.getInt("message_id"),
                                    jsonObject.getInt("user_id"),
                                    jsonObject.getString("username"),
                                    jsonObject.getString("message"),
                                    jsonObject.getString("datetimeutc")
                            )
                    );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("arr size", Integer.toString(items.size()));

            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(first_history) {
                        adapter.prependItems(items);
                        first_history = false;
                    } else {
                        // https://stackoverflow.com/questions/22051556/maintain-scroll-position-when-adding-to-listview-with-reverse-endless-scrolling
                        // https://stackoverflow.com/questions/8276128/retaining-position-in-listview-after-calling-notifydatasetchanged
                        // save index and top position
                        int index = listViewMessages.getFirstVisiblePosition();
                        View v = listViewMessages.getChildAt(0);
                        int top = (v == null) ? 0 : v.getTop();
                        int oldCount = adapter.getCount();

                        // notify dataset changed or re-assign adapter here
                        adapter.prependItems(items);

                        // restore the position of listview
                        listViewMessages.setSelectionFromTop(index + adapter.getCount() - oldCount, top);
                    }

                    // if we haven't reached the start of the messages, release history lock
                    if (items.size() > 0) history_lock = false;


                }
            });
        }
    };

    private class SendMessageTask extends AsyncTask<String, String, Void> {
        private final String message_contents;

        public SendMessageTask(String message_contents) {
            this.message_contents = message_contents;
        }

        @Override
        protected Void doInBackground(String... args) {
            JSONObject inputJson;

            try {
                inputJson = new JSONObject(info.toString());
                inputJson.put("message", message_contents);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            mSocket.emit("send message", inputJson);
            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            Log.i("socket", "sent message to server");
            final MessageAdapter.MessageItem msgItem = new MessageAdapter.MessageItem(user_id, username, message_contents);
            not_on_server_indices.add(adapter.addItem(msgItem));
        }
    }
}
