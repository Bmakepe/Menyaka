package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menyaka.InteractionsActivity;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.Models.Video;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.Utils.UniversalFunctions;
import com.example.menyaka.VideoCommentActivity;
import com.example.menyaka.ViewProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder>{

    private List<Moment> momentItems;
    private Context context;

    //for videos
    boolean isPlaying = false;
    boolean soundPlaying = false;

    boolean isFollowing = false;

    private GetTimeAgo getTimeAgo;
    private UniversalFunctions universalFunctions;

    private FirebaseUser firebaseUser;
    DatabaseReference userRef, retailRef, viewsReference;
    String userProPic, postTimeStamp, postOwner;

    public VideoAdapter(List<Moment> momentItems, Context context) {
        this.momentItems = momentItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        viewsReference = FirebaseDatabase.getInstance().getReference("MenyakaViews");
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);

        Moment listItem = momentItems.get(position);

        getVideoDetails(listItem, holder);
        publisherInfo(holder, listItem.getUsername());
        universalFunctions.isLiked(listItem.getMomentId(), holder.like);
        universalFunctions.nrLikes(holder.likes, listItem.getMomentId());
        universalFunctions.getComments(listItem.getMomentId(), holder.comments);
        universalFunctions.isSaved(listItem.getMomentId(), holder.bookmarkBTN);
        universalFunctions.checkVideoViewCount(holder.videoViews, listItem);
        checkFollow(listItem.getUsername(), holder.followBTN);

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying){
                    holder.postVideoView.pause();
                    isPlaying = false;
                    holder.playBTN.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }else{
                    holder.postVideoView.start();
                    isPlaying = true;
                    holder.playBTN.setImageResource(R.drawable.ic_baseline_pause_24);
                }
            }
        });

        holder.volumeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "You will be able to control volume from here", Toast.LENGTH_SHORT).show();

                /*if (soundPlaying){
                    holder.volumeBTN.setImageResource(R.drawable.ic_baseline_volume_up_24);
                    holder.postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.setVolume(0f, 0f);
                            soundPlaying = false;
                        }
                    });
                }else{
                    holder.volumeBTN.setImageResource(R.drawable.ic_baseline_volume_off_24);
                    holder.postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.setVolume(100f, 100f);
                            soundPlaying = true;
                        }
                    });
                }*/
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(listItem.getMomentId())
                            .child(firebaseUser.getUid()).setValue(true);
                    if(!listItem.getUsername().equals(firebaseUser.getUid()))
                        universalFunctions.addLikesNotification(listItem.getMomentId(), listItem.getUsername());
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(listItem.getMomentId())
                            .child(firebaseUser.getUid()).removeValue();
                    //removeLikeNotification(listItem.getMomentId(), listItem.getUsername());
                }
            }
        });

        holder.itemView.findViewById(R.id.shareArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You will be able to share this moment", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                universalFunctions.addView(listItem.getMomentId());

                Intent momentActivity = new Intent(context, VideoCommentActivity.class);
                momentActivity.putExtra("momentID", listItem.getMomentId());
                context.startActivity(momentActivity);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                universalFunctions.addView(listItem.getMomentId());
                universalFunctions.profileClickOptions(listItem);
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                universalFunctions.addView(listItem.getMomentId());
                universalFunctions.profileClickOptions(listItem);
            }
        });

        holder.itemView.findViewById(R.id.likesArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.likes.getText().toString().equals("Like")){
                    Toast.makeText(context, "No Likes", Toast.LENGTH_SHORT).show();
                }else {
                    Intent likesIntent = new Intent(context, InteractionsActivity.class);
                    likesIntent.putExtra("Interaction", "Likes");
                    likesIntent.putExtra("postID", listItem.getMomentId());
                    context.startActivity(likesIntent);
                }

            }
        });

        /*holder.momentPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("pictureID", listItem.getMomentId());
                context.startActivity(intent);
            }
        });*/

        /*holder.itemView.findViewById(R.id.postPicArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("pictureID", listItem.getMomentId());
                context.startActivity(intent);
            }
        });*/

        holder.postMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.postMenuBTN, Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");

                if (!listItem.getUsername().equals(firebaseUser.getUid())){
                    //popupMenu.getMenu().add(Menu.NONE,3,0,"Add to favourites");

                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                User user = ds.getValue(User.class);

                                if (user.getId().equals(listItem.getUsername())){
                                    String username = user.getUsername();
                                    if (isFollowing)
                                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Unfollow " + username);
                                    else
                                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Follow " + username);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else{
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Delete Post");
                    popupMenu.getMenu().add(Menu.NONE, 3,0,"Post Activity");
                    popupMenu.getMenu().add(Menu.NONE, 4,0,"Promote Post");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        switch (id){
                            case 0:
                                universalFunctions.profileClickOptions(listItem);
                                break;
                            case 1:
                                if (isFollowing){
                                    //unfollow the user
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                            .child("following").child(listItem.getUsername()).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                                            .child("followers").child(firebaseUser.getUid()).removeValue();
                                }else{
                                    //follow the user
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                            .child("following").child(listItem.getUsername()).setValue(true);
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                                    universalFunctions.addFollowNotifications(listItem.getUsername());

                                }
                                break;
                            case 2:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Delete")
                                        .setMessage("Are you sure to delete this post")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                universalFunctions.beginDelete(listItem.getMomentId(), listItem.getImageUrl());

                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                                break;
                            case 3:
                                Intent viewsIntent = new Intent(context, InteractionsActivity.class);
                                viewsIntent.putExtra("Interaction", "Views");
                                viewsIntent.putExtra("postID", listItem.getMomentId());
                                context.startActivity(viewsIntent);
                                break;

                            case 4:
                                Toast.makeText(context, "you will promote this post", Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                throw new IllegalStateException("Unexpected value: " + id);
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFollowing){
                    //unfollow the user
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(listItem.getUsername()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }else{
                    //follow the user
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(listItem.getUsername()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    universalFunctions.addFollowNotifications(listItem.getUsername());

                }
            }
        });

        holder.bookmarkBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.bookmarkBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(listItem.getMomentId()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(listItem.getMomentId()).removeValue();
                }
            }
        });

        holder.itemView.findViewById(R.id.postLocationArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = "https://maps.google.com/maps?saddr=" + listItem.getLatitude() + "," + listItem.getLongitude();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                context.startActivity(intent);
            }
        });
    }

    private void getVideoDetails(Moment listItem, ViewHolder holder){

        holder.postVideoView.setVideoURI(Uri.parse(listItem.getVideoUrl()));
        holder.postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.bufferProgress.setVisibility(View.GONE);
                mp.start();
                isFollowing = true;
                soundPlaying = true;
                mp.setLooping(true);
                holder.playBTN.setImageResource(R.drawable.ic_baseline_pause_24);
                holder.volumeBTN.setImageResource(R.drawable.ic_baseline_volume_up_24);

                float videoRatio = mp.getVideoWidth() / (float)mp.getVideoHeight();
                float screenRatio = holder.postVideoView.getWidth() / (float)holder.postVideoView.getHeight();
                float scale  = videoRatio / screenRatio;
                if (scale >= 1f){
                    holder.postVideoView.setScaleX(scale);
                }else {
                    holder.postVideoView.setScaleY(1f / scale);
                }
            }
        });
        holder.postVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });


        //for posts with videos

        try {
            postTimeStamp = getTimeAgo.getTimeAgo(Long.parseLong(listItem.getTimestamp()), context);
            holder.date.setText(postTimeStamp);
        }catch (NumberFormatException ignored){}

        if (listItem.getMoment_desc().equals("")){
            holder.momentDesc.setVisibility(View.GONE);
        }else {
            holder.momentDesc.setVisibility(View.VISIBLE);
            holder.momentDesc.setText(listItem.getMoment_desc());
        }

        //for checking location of the video post
        try{
            universalFunctions.findAddress(listItem, holder.postCheckIn, holder.locationArea);
        }catch (Exception ignored){}

        //for checking the video post privacy details
        try{
            if (listItem.getPrivacy().equals("Private")){
                holder.itemView.findViewById(R.id.shareArea).setVisibility(View.GONE);
            }
        }catch (NullPointerException ignored){}
    }

    private void publisherInfo(ViewHolder holder, final String id){

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    assert user != null;

                    if (user.getId().equals(id)) {
                        userProPic = user.getImageUrl();

                        if (user.getImageUrl().equals("")) {
                            holder.image_profile.setImageResource(R.drawable.profile_png_1114185);
                        } else {
                            Picasso.get().load(user.getImageUrl()).into(holder.image_profile);
                            //Glide.with(context).load(user.getImageUrl()).into(holder.image_profile);
                        }
                        postOwner = user.getUsername();
                        holder.username.setText(user.getUsername());

                        if (id.equals(firebaseUser.getUid())) {
                            holder.followBTN.setVisibility(View.GONE);
                        }
                    }else{
                        retailRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    Retail retail = ds.getValue(Retail.class);

                                    if (retail.getRetail_id().equals(id)){
                                        userProPic = retail.getImageUrl();

                                        if (retail.getImageUrl().equals("")) {
                                            holder.image_profile.setImageResource(R.drawable.profile_png_1114185);
                                        } else {
                                            Picasso.get().load(retail.getImageUrl()).into(holder.image_profile);
                                            //Glide.with(context).load(retail.getImageUrl()).into(holder.image_profile);
                                        }
                                        postOwner = retail.getRetailName();
                                        holder.username.setText(postOwner);

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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollow(String username, TextView followBTN) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isFollowing = dataSnapshot.child(username).exists();
                if (isFollowing){
                    followBTN.setText("Following");
                }else{
                    followBTN.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return momentItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //for regular posts
        public ImageView momentPic, like, image_profile, comment, postMenuBTN, bookmarkBTN, volumeBTN;
        public TextView comments, date, username, momentDesc, likes, postCheckIn, followBTN, videoViews;
        ProgressBar progressBar;
        CardView postPicArea;
        LinearLayout locationArea;

        //for videos
        VideoView postVideoView;
        ImageView playBTN;

        ProgressBar bufferProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //for regular posts
            momentPic = itemView.findViewById(R.id.postImage);
            progressBar = itemView.findViewById(R.id.progress_load_media);
            momentDesc = itemView.findViewById(R.id.desc);
            like = itemView.findViewById(R.id.post_likes);
            image_profile = itemView.findViewById(R.id.moment_image_user);
            username = itemView.findViewById(R.id.moment_username);
            date = itemView.findViewById(R.id.postDate);
            likes =itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.post_comments);
            comment = itemView.findViewById(R.id.comment);
            postPicArea = itemView.findViewById(R.id.postPicArea);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            bookmarkBTN = itemView.findViewById(R.id.bookmarkBTN);
            postCheckIn = itemView.findViewById(R.id.postCheckIn);
            followBTN = itemView.findViewById(R.id.followBTN);
            videoViews = itemView.findViewById(R.id.videoViewsCounter);
            locationArea = itemView.findViewById(R.id.postLocationArea);
            volumeBTN = itemView.findViewById(R.id.videoVolumeBTN);

            //for video
            postVideoView = itemView.findViewById(R.id.postVideoView);
            playBTN = itemView.findViewById(R.id.videoPlayBTN);
            bufferProgress = itemView.findViewById(R.id.fullLoadingProgressBar);
        }
    }
}
