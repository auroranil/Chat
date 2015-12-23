package com.example.saurabh.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by saurabh on 26/11/15.
 */
public class RoomsFragment extends Fragment {

    public ListView listRooms;
    public SwipeRefreshLayout swipeContainer;
    private String username, session;
    private int user_id;

    RoomAdapter adapter;

    @Override
    public void setArguments(Bundle arguments) {
        this.user_id = arguments.getInt("user_id");
        this.username = arguments.getString("username");
        this.session = arguments.getString("session");
    }

    public RoomAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        listRooms = (ListView) view.findViewById(R.id.list_rooms);

        listRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("room_id", ((RoomAdapter.RoomItem) adapter.getItem(position)).getRoomID());
                intent.putExtra("room_name", ((RoomAdapter.RoomItem) adapter.getItem(position)).getRoomName());
                getActivity().startActivity(intent);
            }
        });

        adapter = new RoomAdapter(getActivity());
        listRooms.setAdapter(adapter);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new FetchRoomsAsyncTask(RoomsFragment.this, username, user_id, session)).execute();
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    // Fetch rooms when activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        (new FetchRoomsAsyncTask(this, username, user_id, session)).execute();
    }
}