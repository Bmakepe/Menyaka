package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.CommentAdapter;
import com.example.menyaka.Common.Comment;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCommentActivity extends AppCompatActivity {

    //for post details
    public TextView momentDesc, username, postTime, pd_postCheckIn;
    public CircleImageView hisProPic, my_image_profile;

    //for getting and posting comments
    private RecyclerView commentsRecycler;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    //for likes, comments, views, menu btn, and share btn
    private ImageView likesBTN, menuBTN, bookmarkBTN;
    private TextView likesCounter, commentCounter;

    private EditText addComment;
    private TextView postBTN;
    private LinearLayout locationArea;
    
    private String momentID, userID, postOwner, postPrivacy, postVideoURL;
    boolean isFollowing = false;
    
    FirebaseUser firebaseUser;
    DatabaseReference momentRef, userRef, retailRef;
    
    private GetTimeAgo timeAgo;
    private UniversalFunctions universalFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_comment);

        ///for post details
        momentDesc = findViewById(R.id.videoComment_desc);
        username = findViewById(R.id.videoComment_username);
        postTime = findViewById(R.id.videoComment_timestamp);
        pd_postCheckIn = findViewById(R.id.videoComment_postCheckIn);
        hisProPic = findViewById(R.id.videoComment_his_moment_image_user);
        my_image_profile = findViewById(R.id.videoComment_my_image_profile);

        commentsRecycler = findViewById(R.id.videoComment_recycler_comments);

        //for likes, comments, view, menuBTN, & shareBTN
        likesBTN = findViewById(R.id.videoComment_post_likes_btn);
        menuBTN = findViewById(R.id.videoComment_postMenuBTN);
        bookmarkBTN = findViewById(R.id.videoComment_bookmark_btn);
        likesCounter = findViewById(R.id.videoComment_likesCounter);
        commentCounter = findViewById(R.id.videoComment_comment_counter);

        //others
        addComment = findViewById(R.id.videoComment_add_comment);
        postBTN = findViewById(R.id.videoComment_postBTN);
        locationArea = findViewById(R.id.pd_postLocationArea);

        Intent intent = getIntent();
        momentID = intent.getStringExtra("momentID");
        
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        momentRef = FirebaseDatabase.getInstance().getReference("Moments");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");

        universalFunctions = new UniversalFunctions(VideoCommentActivity.this);
        
        getMyDetails();
        readComments();
        universalFunctions.getComments(momentID, commentCounter);
        getPostDetails();
        universalFunctions.isLiked(momentID, likesBTN);
        universalFunctions.nrLikes(likesCounter, momentID);
        universalFunctions.isSaved(momentID, bookmarkBTN);
        checkFollow();

        commentsRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentsRecycler.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, momentID);
        commentsRecycler.setAdapter(commentAdapter);

        bookmarkBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(bookmarkBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(momentID).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(momentID).removeValue();
                }
            }
        });

        menuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(VideoCommentActivity.this, menuBTN, Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");

                if (!userID.equals(firebaseUser.getUid())){
                    //popupMenu.getMenu().add(Menu.NONE,3,0,"Add to favourites");

                    if (isFollowing)
                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Unfollow " + postOwner);
                    else
                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Follow " + postOwner);
                }else{
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Delete Post");
                    popupMenu.getMenu().add(Menu.NONE, 3,0,"Post Details");
                    popupMenu.getMenu().add(Menu.NONE, 4,0,"Promote Post");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        switch (id){
                            case 0:
                                profileClickOptions();
                                break;

                            case 1:
                                if (isFollowing){
                                    //unfollow the user
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                            .child("following").child(userID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(userID)
                                            .child("followers").child(firebaseUser.getUid()).removeValue();
                                }else{
                                    //follow the user
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                            .child("following").child(userID).setValue(true);
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(userID)
                                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                                    universalFunctions.addFollowNotifications(userID);
                                }
                                break;

                            case 2:
                                AlertDialog.Builder builder = new AlertDialog.Builder(VideoCommentActivity.this);
                                builder.setTitle("Delete")
                                        .setMessage("Are you sure to delete this post")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                universalFunctions.beginDelete(momentID, postVideoURL);

                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                                break;

                            case 3:
                                Intent viewsIntent = new Intent(VideoCommentActivity.this, InteractionsActivity.class);
                                viewsIntent.putExtra("Interaction", "Views");
                                viewsIntent.putExtra("postID", momentID);
                                startActivity(viewsIntent);
                                break;

                            case 4:
                                Toast.makeText(VideoCommentActivity.this, "you will be able to promote this post", Toast.LENGTH_SHORT).show();
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

        hisProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                profileClickOptions();
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                profileClickOptions();
            }
        });

        likesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(likesBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID)
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!userID.equals(firebaseUser.getUid()))
                        universalFunctions.addVideoNotification(userID, momentID);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID)
                            .child(firebaseUser.getUid()).removeValue();
                    //removeLikesNotification(authorId, momentId);
                }
            }
        });

        postBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addComment.getText().toString().equals("")){
                    Toast.makeText(VideoCommentActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        findViewById(R.id.videoComment_backBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void profileClickOptions() {

        if(userID.equals(firebaseUser.getUid())){
            startActivity(new Intent(VideoCommentActivity.this, ProfileActivity.class));
        }else {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        if (user.getId().equals(userID)){

                            Intent intent = new Intent(VideoCommentActivity.this, ViewProfileActivity.class);
                            intent.putExtra("userID", userID);
                            startActivity(intent);

                        }else{
                            retailRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        Retail retail =  ds.getValue(Retail.class);

                                        if (retail.getRetail_id().equals(userID)){

                                            Intent shopIntent = new Intent(VideoCommentActivity.this, ShopDetailsActivity.class);
                                            shopIntent.putExtra("storeId", retail.getRetail_id());
                                            startActivity(shopIntent);

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
    }

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(momentID);
        final String timestamp = String.valueOf(System.currentTimeMillis());

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("commentID", key);
        hashMap.put("comment", addComment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("timeStamp", timestamp);

        reference.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(!userID.equals(firebaseUser.getUid())){
                    sendCommentNotification(addComment);
                }
                addComment.setText("");
            }
        });
    }

    private void sendCommentNotification(EditText addComment) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userID);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "commented: " + addComment.getText().toString());
        hashMap.put("postid", momentID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        reference.child(key).setValue(hashMap);
    }

    private void getPostDetails() {
        timeAgo = new GetTimeAgo();

        momentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    if (moment.getMomentId().equals(momentID)){
                        userID = moment.getUsername();
                        momentDesc.setText(moment.getMoment_desc());

                        postVideoURL = moment.getVideoUrl();

                        try {

                            universalFunctions.findAddress(moment, pd_postCheckIn, locationArea);
                        }catch (NullPointerException ignored){}

                        try{
                            String timeTime = timeAgo.getTimeAgo(Long.parseLong(moment.getTimestamp()), VideoCommentActivity.this);
                            postTime.setText(timeTime);
                        }catch (NumberFormatException ignored){}


                        postPrivacy = moment.getPrivacy();
                        try{
                            if (postPrivacy.equals("Private")){
                                findViewById(R.id.pd_shareArea).setVisibility(View.GONE);
                            }
                        }catch (NullPointerException ignored){}

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot data : snapshot.getChildren()){
                                    User user = data.getValue(User.class);

                                    assert user != null;
                                    if (user.getId().equals(userID)) {

                                        postOwner = user.getUsername();
                                        username.setText(user.getUsername());

                                        try {
                                            Picasso.get().load(user.getImageUrl()).into(hisProPic);
                                        } catch (NullPointerException ignored) {}
                                    }else{
                                        retailRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    Retail retail = ds.getValue(Retail.class);

                                                    if (retail.getRetail_id().equals(userID)){

                                                        postOwner = retail.getRetailName();
                                                        username.setText(retail.getRetailName());

                                                        try {
                                                            Picasso.get().load(retail.getImageUrl()).into(hisProPic);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readComments() {

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(momentID);

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMyDetails() {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    
                    if (user.getId().equals(firebaseUser.getUid())){
                        try {
                            Picasso.get().load(user.getImageUrl()).into(my_image_profile);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                isFollowing = dataSnapshot.child(userID).exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}