package com.example.saurabh.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
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

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {
    private static final int ROOM = 0, FRIEND = 1;
    private ListView listViewMessages;
    private MessageAdapter adapter;
    private EditText txtMessage;
    private LinearLayout footerView;
    private TextView usersTypingTextView;
    private TextView isTypingTextView;
    private boolean typing = false;
    private boolean history_lock = false;
    private String earliest_datetime_utc;

    private int room_id;

    private final ArrayList<String> usersTyping = new ArrayList<>();

    private SharedPreferences userSharedPreferences;
    private Intent intent;

    private JSONObject info;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(WelcomeActivity.url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String getUsername() {
        if(userSharedPreferences.contains("username")) {
            // empty default string will never be returned by default
            return userSharedPreferences.getString("username", "");
        }

        // if not stored in shared preferences, try to get it from intent
        // will be null if string is not set
        return intent.getStringExtra("username");
    }

    private int getUserID() {
        if(userSharedPreferences.contains("user_id")) {
            // empty default int will never be returned by default
            return userSharedPreferences.getInt("user_id", -1);
        }

        // if not stored in shared preferences, try to get it from intent
        // will be null if string is not set
        return intent.getIntExtra("user_id", -1);
    }

    private String getSession() {
        if(userSharedPreferences.contains("session")) {
            // empty default string will never be returned by default
            return userSharedPreferences.getString("session", "");
        }

        // if not stored in shared preferences, try to get it from intent
        // will be null if string is not set
        return intent.getStringExtra("session");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userSharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        intent = getIntent();
        int user_id = getUserID();
        String username = getUsername();
        String session = getSession();
        String room_name = intent.getStringExtra("room_name");
        // TODO: deal with default value
        room_id = intent.getIntExtra("room_id", -1);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setTitle("Room: " + room_name);
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
        } catch(JSONException e) {
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
        footerView.setVisibility(View.INVISIBLE);

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
                if(adapter.getCount() == 0) {
                    return;
                }

                if (firstVisibleItem == 0) {
                    // check if we reached the top or bottom of the list
                    View v = listViewMessages.getChildAt(0);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        // reached the top:
                        Log.d("ChatActivity", "history_lock=" + history_lock);
                        if(!history_lock) {
                            try {
                                JSONObject json = new JSONObject(info.toString());
                                if(earliest_datetime_utc != null) {
                                    json.put("datetimeutc", earliest_datetime_utc);
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

                if(!message.isEmpty() && !typing) {
                    typing = true;
                    mSocket.emit("typing", info);
                } else if(message.isEmpty() && typing) {
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
                if(!msg.isEmpty()) {
                    new SendMessageTask().execute(msg);
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
            int user_id = -1;
            String username = "", message = "";
            String datetimeutc = "";
            try {
                json = (JSONObject) args[0];

                user_id = json.getInt("user_id");
                username = json.getString("username");
                message = json.getString("message");
                datetimeutc = json.getString("datetimeutc");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final MessageAdapter.MessageItem msgItem = new MessageAdapter.MessageItem(user_id, username, message, datetimeutc);
            Log.i("message", msgItem.toString());
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.addItem(msgItem);
                }
            });
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
                    usersTypingTextView.setText(usersTypingStr.substring(1, usersTypingStr.length()-1));

                    if(usersTyping.size() == 1) {
                        // show view
                        footerView.setVisibility(View.VISIBLE);
                        isTypingTextView.setText(" is typing...");
                    }

                    if(usersTyping.size() == 2) {
                        isTypingTextView.setText(" are typing...");
                    }
                }
            });
        }
    };

    private final Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            usersTyping.remove((String) args[0]);

            if(usersTyping.isEmpty()) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String usersTypingStr = usersTyping.toString();
                        usersTypingTextView.setText(usersTypingStr.substring(1, usersTypingStr.length()-1));

                        if(usersTyping.size() == 1) {
                            isTypingTextView.setText(" is typing...");
                        }

                        if(usersTyping.isEmpty()) {
                            // hide view
                            footerView.setVisibility(View.INVISIBLE);
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
                if(json.has("earliest_datetimeutc")) {
                    earliest_datetime_utc = json.getString("earliest_datetimeutc");
                }
                for(int i = 0; i < arr.length(); i++) {
                    Log.i("Index",  Integer.toString(i));
                    items.add(new MessageAdapter.MessageItem(arr.getJSONObject(i).getInt("user_id"), arr.getJSONObject(i).getString("username"), arr.getJSONObject(i).getString("message"), arr.getJSONObject(i).getString("datetimeutc")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("arr size", Integer.toString(items.size()));

            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.prependItems(items);
                    // if we haven't reached the start of the messages, release history lock
                    if(items.size() > 0) history_lock = false;
                }
            });
        }
    };

    private class SendMessageTask extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... args) {
            JSONObject inputJson;

            try {
                inputJson = new JSONObject(info.toString());
                inputJson.put("message", args[0]);
            } catch(JSONException e) {
                e.printStackTrace();
                return null;
            }

            mSocket.emit("send message", inputJson, new Ack() {
                @Override
                public void call(Object... args) {
                    final String ack_message = (String) args[0];
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(ack_message != null)
                                Log.i("socket", ack_message);
                        }
                    });
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            Log.i("socket", "sent message to server");
        }
    }
}
