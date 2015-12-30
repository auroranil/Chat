package com.example.saurabh.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.saurabh.chat.R;
import com.example.saurabh.chat.layouts.StatusLayout;

public class FriendsListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        StatusLayout statusLayout = (StatusLayout) v.findViewById(R.id.layout_status_friendlist);
        statusLayout.setError("You haven't added any friends yet.");

        ListView friendsListView = (ListView) v.findViewById(R.id.listView_friends);
        friendsListView.setVisibility(View.GONE);

        return v;
    }
}
