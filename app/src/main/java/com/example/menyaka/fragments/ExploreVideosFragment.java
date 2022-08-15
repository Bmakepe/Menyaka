package com.example.menyaka.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.menyaka.Adapters.ExploreVideoAdapter;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class ExploreVideosFragment extends Fragment {

    RecyclerView videosRecycler;

    ExploreVideoAdapter adapter;
    ArrayList<Moment> exploreVideos;

    DatabaseReference videoReference;

    ImageView filterBTN;
    EditText searchArea;


    public ExploreVideosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        videosRecycler = view.findViewById(R.id.exploreRecycler);
        filterBTN = view.findViewById(R.id.searchFilterIcon);
        searchArea = view.findViewById(R.id.search_bar);

        exploreVideos = new ArrayList<>();

        videoReference = FirebaseDatabase.getInstance().getReference("Moments");

        videosRecycler.hasFixedSize();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        videosRecycler.setLayoutManager(layoutManager);

        getExploreVideos();

        searchArea.setHint("Search Menyaka");
        filterBTN.setVisibility(View.GONE);

        return view;
    }

    private void getExploreVideos() {

        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                exploreVideos.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);
                    try{
                        assert moment != null;
                        if (!moment.getPrivacy().equals("Private")){
                            if (moment.getPostType().equals("videoPost"))
                                exploreVideos.add(moment);
                        }
                    }catch (NullPointerException ignored){}

                    Collections.shuffle(exploreVideos);

                    adapter = new ExploreVideoAdapter(getActivity(), exploreVideos);
                    videosRecycler.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
