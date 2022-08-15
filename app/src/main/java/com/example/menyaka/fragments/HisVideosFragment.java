package com.example.menyaka.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.menyaka.Adapters.ExploreVideoAdapter;
import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.VideoAdapter;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HisVideosFragment extends Fragment {

    String hisUserID;

    //for retrieving posts
    private RecyclerView postsRecycler;
    private ExploreVideoAdapter postAdapter;
    private ArrayList<Moment> postList;

    private DatabaseReference videosRef;

    private ProgressBar postsLoader;

    public HisVideosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        hisUserID = getArguments().getString("hisUserID");

        //for posts on timeline
        postsRecycler = view.findViewById(R.id.fragmentRecycler);
        postsLoader = view.findViewById(R.id.fragmentLoader);

        videosRef = FirebaseDatabase.getInstance().getReference("Moments");

        //declaration of post lists
        postList = new ArrayList<>();

        postsRecycler.hasFixedSize();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        postsRecycler.setLayoutManager(layoutManager);

        getHisVideos();


        return view;
    }

    private void getHisVideos() {

        videosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    assert moment != null;
                    if (moment.getPostType().equals("videoPost")) {

                        if (moment.getUsername().equals(hisUserID))
                            postList.add(moment);
                    }
                }

                postAdapter = new ExploreVideoAdapter(getActivity(), postList);
                Collections.reverse(postList);
                postsRecycler.setAdapter(postAdapter);
                postsLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}