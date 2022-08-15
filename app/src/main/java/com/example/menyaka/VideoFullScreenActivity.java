package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.Utils.VideoProgress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoFullScreenActivity extends AppCompatActivity {

    //for videos
    VideoView postVideoView;
    RelativeLayout postVideoArea;
    ImageView playBTN, volumeBTN, videoMenuBTN, toolbarMenu, backBTN;
    TextView totalDuration, onGoingDuration, titleHeader;
    ProgressBar bufferProgress, videoSeekbar;
    CircleImageView profileImage;

    boolean isPlaying = false;

    String videoURL_ID, postPrivacy;

    DatabaseReference menyakaVideosRef, adVideosRef, userRef, storeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_video_full_screen);

        toolbarMenu = findViewById(R.id.fullScreenVideoMenu);
        backBTN = findViewById(R.id.fullScreenVideoBackBTN);
        titleHeader = findViewById(R.id.fullScreenVideoTitle);
        profileImage = findViewById(R.id.fullScreenVideo_ProfilePic);

        postVideoView = findViewById(R.id.fullScreenVideoView);
        postVideoArea = findViewById(R.id.fullScreenVideoArea);
        playBTN = findViewById(R.id.fullScreenVideoPlayBTN);
        volumeBTN = findViewById(R.id.fullScreenVolumeBTN);
        videoMenuBTN = findViewById(R.id.fullScreenVideoMenuBTN);
        totalDuration = findViewById(R.id.fullScreenTotalDuration);
        onGoingDuration = findViewById(R.id.fullScreenOnGoingDuration);
        bufferProgress = findViewById(R.id.fullScreenBufferProgress);
        videoSeekbar = findViewById(R.id.fullScreenVideoSeekbar);

        Intent intent = getIntent();
        videoURL_ID = intent.getStringExtra("videoURL_ID");

        menyakaVideosRef = FirebaseDatabase.getInstance().getReference("Moments");
        adVideosRef = FirebaseDatabase.getInstance().getReference("AdBanners");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");

        getMedia();

        playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying){
                    postVideoView.pause();
                    isPlaying = false;
                    playBTN.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }else{
                    postVideoView.start();
                    isPlaying = true;
                    playBTN.setImageResource(R.drawable.ic_baseline_pause_24);
                }

            }
        });

        videoMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final PopupMenu popupMenu = new PopupMenu(VideoFullScreenActivity.this, videoMenuBTN, Gravity.END);

                if(postPrivacy.equals("Public")){
                    popupMenu.getMenu().add(Menu.NONE, 0,0,"Download");
                }


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if (id == 0) {
                            Toast.makeText(VideoFullScreenActivity.this, "You will be able to download this video", Toast.LENGTH_SHORT).show();
                        } else {
                            throw new IllegalStateException("Unexpected value: " + id);
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getMedia() {
        menyakaVideosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    if (moment.getMomentId().equals(videoURL_ID)){
                        if (moment.getPostType().equals("videoPost")){
                            postPrivacy = moment.getPrivacy();
                            getVideoURI(moment.getVideoUrl());

                            userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        User user = ds.getValue(User.class);

                                        if (user.getId().equals(moment.getUsername())){
                                            titleHeader.setText(user.getUsername());

                                            try{
                                                Picasso.get().load(user.getImageUrl()).into(profileImage);
                                            }catch (NullPointerException ignored){}
                                        }else{
                                            storeRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                        Retail retail = ds.getValue(Retail.class);

                                                        if (retail.getRetail_id().equals(moment.getUsername())) {

                                                            titleHeader.setText(retail.getRetailName());

                                                            try {
                                                                Picasso.get().load(retail.getImageUrl()).into(profileImage);
                                                            } catch (NullPointerException ignored) {}
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }else{
                        adVideosRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    BannerAds bannerAds = ds.getValue(BannerAds.class);

                                    if (bannerAds.getBannerID().equals(videoURL_ID)){
                                        if (bannerAds.getAd_type().equals("mediaAd")){
                                            getVideoURI(bannerAds.getBannerMedia());

                                            storeRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                        Retail retail = ds.getValue(Retail.class);

                                                        if (retail.getRetail_id().equals(bannerAds.getRetail_ID())){
                                                            titleHeader.setText(retail.getRetailName() + " Sponsored Ad");

                                                            try{
                                                                Picasso.get().load(retail.getImageUrl()).into(profileImage);
                                                            }catch (NullPointerException ignored){}
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoURI(String URI) {

        postVideoView.setVideoURI(Uri.parse(URI));

        postVideoView.setOnPreparedListener(mediaPlayer -> {

            mediaPlayer.setVolume(0f, 0f);
            isPlaying = true;
            mediaPlayer.setLooping(true);

        });

        postVideoView.start();
        playBTN.setImageResource(R.drawable.ic_baseline_pause_24);

        postVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {

                if (i == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                    bufferProgress.setVisibility(View.VISIBLE);
                }else if (i == MediaPlayer.MEDIA_INFO_BUFFERING_END){
                    bufferProgress.setVisibility(View.GONE);
                }

                return false;
            }
        });
    }
}