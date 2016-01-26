package com.example.saurabh.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.saurabh.chat.R;
import com.example.saurabh.chat.activities.ChatActivity;
import com.example.saurabh.chat.adapters.FriendsAdapter;
import com.example.saurabh.chat.layouts.StatusLayout;
import com.example.saurabh.chat.network.FetchFriendsAsyncTask;

public class FriendsListFragment extends Fragment {

    ListView friendsListView;
    public StatusLayout statusLayout;
    FriendsAdapter friendsAdapter;
    public SwipeRefreshLayout swipeContainer;
    private String username, session;
    private int user_id;

    @Override
    public void setArguments(Bundle arguments) {
        this.user_id = arguments.getInt("user_id");
        this.username = arguments.getString("username");
        this.session = arguments.getString("session");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        statusLayout = (StatusLayout) v.findViewById(R.id.layout_status_friendlist);
        statusLayout.setLoading();

        friendsListView = (ListView) v.findViewById(R.id.listView_friends);
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(friendsAdapter.getItem(position) instanceof FriendsAdapter.FriendItem) {
                    FriendsAdapter.FriendItem friendItem = ((FriendsAdapter.FriendItem) friendsAdapter.getItem(position));
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("friend_username", friendItem.getUsername());
                    intent.putExtra("friend_user_id", friendItem.getUserID());
                    intent.putExtra("type", ChatActivity.FRIEND);
                    getActivity().startActivity(intent);
                }
            }
        });

        friendsAdapter = new FriendsAdapter(this);
        friendsListView.setAdapter(friendsAdapter);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new FetchFriendsAsyncTask(FriendsListFragment.this, username, user_id, session)).execute();
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
            }
        });
        swipeContainer.setVisibility(View.GONE);

        return v;
    }

    public void refresh() {
        (new FetchFriendsAsyncTask(FriendsListFragment.this, username, user_id, session)).execute();
    }

    public FriendsAdapter getAdapter() {
        return friendsAdapter;
    }

    // Fetch friend list when activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        (new FetchFriendsAsyncTask(FriendsListFragment.this, username, user_id, session)).execute();
        swipeContainer.setVisibility(View.GONE);
        statusLayout.setLoading();
        statusLayout.setActionButton("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new FetchFriendsAsyncTask(FriendsListFragment.this, username, user_id, session)).execute();
            }
        });
    }
}
