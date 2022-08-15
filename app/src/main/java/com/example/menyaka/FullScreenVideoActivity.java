package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.menyaka.Adapters.VideoAdapter;
import com.example.menyaka.Models.Moment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FullScreenVideoActivity extends AppCompatActivity {

    ViewPager2 videosViewPager;
    DatabaseReference momentRef;

    List<Moment> momentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);

        videosViewPager = findViewById(R.id.viewPagerVideos);
        momentList = new ArrayList<>();

        momentRef = FirebaseDatabase.getInstance().getReference("Moments");
        momentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                momentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    if (moment.getPostType().equals("videoPost")){
                        momentList.add(moment);
                    }

                    //videosViewPager.setAdapter(new VideoAdapter(momentList, FullScreenVideoActivity.this));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}