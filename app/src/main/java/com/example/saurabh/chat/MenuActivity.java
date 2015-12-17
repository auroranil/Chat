package com.example.saurabh.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MenuActivity extends AppCompatActivity {
    private String username, session;
    private int user_id = -1;
    RoomsFragment roomsFragment;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private SharedPreferences userSharedPreferences;
    private Intent intent;

    private boolean isLoggedIn() {
        return userSharedPreferences.contains("username") && userSharedPreferences.contains("session");
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
        setContentView(R.layout.activity_menu);

        intent = getIntent();
        userSharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        username = getUsername();
        user_id = getUserID();
        session = getSession();

        roomsFragment = new RoomsFragment();
        Bundle roomsFragmentArguments = new Bundle();
        roomsFragmentArguments.putInt("user_id", user_id);
        roomsFragmentArguments.putString("username", username);
        roomsFragmentArguments.putString("session", session);
        roomsFragment.setArguments(roomsFragmentArguments);
        (new FetchRoomsAsyncTask(roomsFragment, username, user_id, session)).execute();

        CollectionPagerAdapter mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), roomsFragment);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager_menu);
        mViewPager.setAdapter(mCollectionPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        if(isLoggedIn() && intent.getBooleanExtra("returning user", false)) {
            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_menu);

            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Signed in as " + username, Snackbar.LENGTH_LONG)
                    .setAction("Not " + username + "?", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Logout(MenuActivity.this, username, session);
                        }
                    });

            snackbar.show();
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateRoomDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);

        // personalise menu item for viewing user's own profile
        menu.findItem(R.id.action_user_profile).setTitle("View " + username + "'s profile");

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
        } else if(id == R.id.action_user_profile) {
            Intent userProfileIntent = new Intent(MenuActivity.this, UserProfileActivity.class);
            userProfileIntent.putExtra("username", username);
            userProfileIntent.putExtra("session", session);
            startActivity(userProfileIntent);
        } else if(id == R.id.action_create_room) {
            showCreateRoomDialog();

            return true;
        } else if(id == R.id.action_logout) {
            new Logout(MenuActivity.this, username, session);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            new Logout(MenuActivity.this, username, session);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void showCreateRoomDialog() {
        AlertDialog.Builder createRoomDialog = new AlertDialog.Builder(MenuActivity.this);
        createRoomDialog.setTitle("Create room");

        final EditText input = new EditText(MenuActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        input.setLayoutParams(lp);
        input.setHint("Must be between 1-20 characters");

        createRoomDialog.setView(input);

        createRoomDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String room = input.getText().toString();
                if(!room.isEmpty() && room.length() <= 20) {
                    (new CreateRoomAsyncTask(MenuActivity.this, user_id, username, session, room)).execute();
                } else {
                    Toast.makeText(MenuActivity.this, "Room name must be between 1-20 characters long.", Toast.LENGTH_LONG).show();
                }
            }
        });

        createRoomDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        createRoomDialog.show();
    }

    class CollectionPagerAdapter extends FragmentPagerAdapter {
        private static final int FRAGMENT_ROOMS = 0, FRAGMENT_FRIENDS = 1;

        RoomsFragment roomsFragment;

        public CollectionPagerAdapter(FragmentManager fm, RoomsFragment roomsFragment) {
            super(fm);

            this.roomsFragment = roomsFragment;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case FRAGMENT_ROOMS:
                    return roomsFragment;
                case FRAGMENT_FRIENDS:
                    return new FriendsListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case FRAGMENT_ROOMS:
                    return "Rooms";
                case FRAGMENT_FRIENDS:
                    return "Friends";
                default:
                    return "Null";
            }
        }
    }
}