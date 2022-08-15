package com.example.menyaka.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.VideoAdapter;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class VideosFragment extends Fragment {

    //for retrieving posts
    private List<Moment> postList;
    private ViewPager2 homePager;

    private FirebaseUser firebaseUser;
    private DatabaseReference followingRef, postsRef;

    //followers
    private List<String> followingList;

    private ProgressBar postsLoader;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_videos, container, false);

        //for posts on timeline
        postsLoader = view.findViewById(R.id.videoPostLoader);
        homePager = view.findViewById(R.id.homePagerVideos);

        //database declarations
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postsRef = FirebaseDatabase.getInstance().getReference("Moments");
        followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        //declaration of post lists
        postList = new ArrayList<>();

        //declaration of following lists
        followingList = new ArrayList<>();

        checkFollowing();

        return view;
    }


    private void checkFollowing() {
        followingRef.keepSynced(true);
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                loadFollowingStores();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "there was an error with loading posts of people you follow ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowingStores() {
        final DatabaseReference favStores = FirebaseDatabase.getInstance().getReference()
                .child("MyStores").child(firebaseUser.getUid());
        favStores.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        followingList.add(ds.getKey());

                    }
                }
                loadPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPosts() {

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    assert moment != null;
                    if (moment.getPostType().equals("videoPost")) {
                        for (String ID : followingList) {
                            if (moment.getUsername().equals(ID))
                                postList.add(moment);
                        }
                        if (moment.getUsername().equals(firebaseUser.getUid()))
                            postList.add(moment);
                    }
                }

                Collections.reverse(postList);
                homePager.setAdapter(new VideoAdapter(postList, getContext()));
                postsLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
