package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.menyaka.Adapters.ExploreVideoAdapter;
import com.example.menyaka.Adapters.VideoAdapter;
import com.example.menyaka.Models.Moment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoMomentActivity extends AppCompatActivity {

    private List<Moment> videoList;
    private ViewPager2 momentPager;

    private DatabaseReference videoReference;
    String selectedMoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_moment);

        momentPager = findViewById(R.id.momentPagerVideos);

        Intent intent = getIntent();
        selectedMoment = intent.getStringExtra("moment");

        videoReference = FirebaseDatabase.getInstance().getReference("Moments");
        videoList = new ArrayList<>();

        Toast.makeText(this, "moment " + selectedMoment + " will display first", Toast.LENGTH_SHORT).show();

        //videoList.add(selectedMoment);

        getVideos();

        findViewById(R.id.videoMomentBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getVideos() {

        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);
                    try{
                        assert moment != null;
                        if (!moment.getPrivacy().equals("Private")){
                            if (moment.getPostType().equals("videoPost"))
                                if (moment.getMomentId().equals(selectedMoment))
                                    videoList.add(moment);
                        }

                        if (!moment.getPrivacy().equals("Private")){
                            if (moment.getPostType().equals("videoPost"))
                                if (!moment.getMomentId().equals(selectedMoment))
                                    videoList.add(moment);
                        }


                    }catch (NullPointerException ignored){}

                    //Collections.shuffle(videoList);
                    momentPager.setAdapter(new VideoAdapter(videoList, VideoMomentActivity.this));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}