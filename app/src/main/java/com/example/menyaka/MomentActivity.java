package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.menyaka.Adapters.CommentAdapter;
import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.UserAdapter;
import com.example.menyaka.Common.Comment;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Notification;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.Utils.UniversalFunctions;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MomentActivity extends AppCompatActivity {

    //for post details
    public ImageView momentPic;
    public TextView momentDesc, username, postTime, pd_postCheckIn;
    public CircleImageView hisProPic, my_image_profile;
    ProgressBar progressBar;

    //for getting and posting comments
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    
    //for likes, comments, views, menu btn, and share btn
    private ImageView likesBTN, menuBTN, bookmarkBTN;
    private TextView likesCounter, commentCounter;

    EditText addComment;
    TextView postBTN;
    LinearLayout locationArea;

    String momentId, userID, postImage, authorId, postOwner, postPrivacy;
    boolean isFollowing = false;
    double latitude, longitude;

    FirebaseUser firebaseUser;

    DatabaseReference momentReference, userRef, retailRef;

    public GetTimeAgo timeAgo;
    public UniversalFunctions universalFunctions;

    //for shared moment area
    public CircleImageView sharedPostProPic;
    public TextView sharedPostUsername, sharedPostDate, sharedPostCheckIn, sharedPostDesc;
    public ProgressBar sharedProgressBar;
    public LinearLayout sharedLocationArea;
    public ImageView sharedPostImage;
    public CardView sharedPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment);

        momentPic = findViewById(R.id.pd_postImage);
        progressBar = findViewById(R.id.pd_progress_load_photo);
        momentDesc = findViewById(R.id.pd_desc);
        hisProPic = findViewById(R.id.his_moment_image_user);
        username = findViewById(R.id.pd_moment_username);
        postTime = findViewById(R.id.pd_timestamp);
        bookmarkBTN = findViewById(R.id.pd_bookmark_btn);
        pd_postCheckIn = findViewById(R.id.pd_postCheckIn);

        addComment = findViewById(R.id.add_comment);
        my_image_profile = findViewById(R.id.my_image_profile);
        postBTN = findViewById(R.id.post);
        locationArea = findViewById(R.id.pd_postLocationArea);

        //for likes, comments, view, and share
        likesBTN = findViewById(R.id.pd_post_likes_btn);
        likesCounter = findViewById(R.id.likesCounter);
        commentCounter = findViewById(R.id.pd_comment_counter);
        menuBTN = findViewById(R.id.pd_postMenuBTN);

        //for shared moments area
        sharedPostProPic = findViewById(R.id.shared_moment_image_user);
        sharedPostUsername = findViewById(R.id.shared_moment_username);
        sharedPostDate = findViewById(R.id.shared_moment_postDate);
        sharedPostCheckIn = findViewById(R.id.shared_moment_postCheckIn);
        sharedPostDesc = findViewById(R.id.shared_moment_post_desc);
        sharedProgressBar = findViewById(R.id.shared_moment_progress_load_media);
        sharedLocationArea = findViewById(R.id.shared_moment_LocationArea);
        sharedPostImage = findViewById(R.id.shared_moment_postImage);
        sharedPost = findViewById(R.id.shared_moment_post_item);

        recyclerView = findViewById(R.id.recycler_comments);

        Intent intent = getIntent();
        momentId = intent.getStringExtra("momentId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        momentReference = FirebaseDatabase.getInstance().getReference("Moments");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        universalFunctions = new UniversalFunctions(MomentActivity.this);
        timeAgo = new GetTimeAgo();

        getMyDetails();
        readComments();
        universalFunctions.getComments(momentId, commentCounter);
        getPostDetails();
        universalFunctions.isLiked(momentId, likesBTN);
        universalFunctions.nrLikes(likesCounter, momentId);
        universalFunctions.isSaved(momentId, bookmarkBTN);
        checkFollow();

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, momentId);
        recyclerView.setAdapter(commentAdapter);

        hisProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(userID.equals(firebaseUser.getUid())){
                    startActivity(new Intent(MomentActivity.this, ProfileActivity.class));
                }else {
                    Intent intent = new Intent(MomentActivity.this, ViewProfileActivity.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                }
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(userID.equals(firebaseUser.getUid())){
                    startActivity(new Intent(MomentActivity.this, ProfileActivity.class));
                }else {
                    Intent intent = new Intent(MomentActivity.this, ViewProfileActivity.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                }
            }
        });

        likesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(likesBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(momentId)
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!userID.equals(firebaseUser.getUid()))
                        universalFunctions.addLikesNotification(authorId, momentId);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(momentId)
                            .child(firebaseUser.getUid()).removeValue();
                    //removeLikesNotification(authorId, momentId);
                }
            }
        });

        postBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addComment.getText().toString().equals("")){
                    Toast.makeText(MomentActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        findViewById(R.id.pd_backBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bookmarkBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(bookmarkBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(momentId).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(momentId).removeValue();
                }
            }
        });

        menuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(MomentActivity.this, menuBTN, Gravity.END); popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");

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
                                if(userID.equals(firebaseUser.getUid())){
                                    startActivity(new Intent(MomentActivity.this, ProfileActivity.class));
                                }else {
                                    Intent intent = new Intent(MomentActivity.this, ViewProfileActivity.class);
                                    intent.putExtra("userID", userID);
                                    startActivity(intent);
                                }
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MomentActivity.this);
                                builder.setTitle("Delete")
                                        .setMessage("Are you sure to delete this post")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                universalFunctions.beginDelete(momentId, postImage);

                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                                break;

                            case 3:
                                Intent viewsIntent = new Intent(MomentActivity.this, InteractionsActivity.class);
                                viewsIntent.putExtra("Interaction", "Views");
                                viewsIntent.putExtra("postID", momentId);
                                startActivity(viewsIntent);
                                break;

                            case 4:
                                Toast.makeText(MomentActivity.this, "you will be able to promote this post", Toast.LENGTH_SHORT).show();
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

        findViewById(R.id.pd_postPicArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MomentActivity.this, FullScreenImageActivity.class);
                intent.putExtra("pictureID", momentId);
                startActivity(intent);
            }
        });

        findViewById(R.id.pd_shareArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(MomentActivity.this, SharePostActivity.class);
                shareIntent.putExtra("postID", momentId);
                startActivity(shareIntent);
            }
        });

        findViewById(R.id.pd_likesArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent likesIntent = new Intent(MomentActivity.this, InteractionsActivity.class);
                likesIntent.putExtra("Interaction","Likes");
                likesIntent.putExtra("postID", momentId);
                startActivity(likesIntent);
            }
        });

        findViewById(R.id.pd_postLocationArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = "https://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                startActivity(intent);

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

    private void removeLikesNotification(String authorId, String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(authorId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Notification notification = ds.getValue(Notification.class);

                    if (notification.getPostid().equals(postId)){
                        String key = notification.getNotificationID();
                        reference.child(key).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(postImage);
        final String timestamp = String.valueOf(System.currentTimeMillis());

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Downloading file");
        pd.setMessage("Downloading, Please Wait...");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();

        final File rootPath = new File(Environment.getExternalStorageDirectory(), "MENYAKA");

        if (!rootPath.exists()){
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, "MENYAKA_" + timestamp +".jpg");

        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ", ";local item created created " + localFile.toString());

                /*if(!isVisible()){
                    return;
                }*/

                if (localFile.canRead()){
                    pd.dismiss();
                }

                Toast.makeText(MomentActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMomentPic(ImageView fullScreenImage, TextView fullScreenHeader) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Moments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    assert moment != null;
                    if(moment.getMomentId().equals(momentId)){
                        fullScreenHeader.setText("");

                        try{
                            Picasso.get().load(moment.getImageUrl()).into(fullScreenImage);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostDetails() {

        momentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    assert moment != null;
                    if(moment.getMomentId().equals(momentId)){

                        userID = moment.getUsername();
                        postImage = moment.getImageUrl();
                        momentDesc.setText(moment.getMoment_desc());
                        authorId = moment.getUsername();

                        try {
                            universalFunctions.findAddress(moment, pd_postCheckIn, locationArea);
                        }catch (NullPointerException ignored){}

                        try{
                            String timeTime = timeAgo.getTimeAgo(Long.parseLong(moment.getTimestamp()), MomentActivity.this);
                            postTime.setText(timeTime);
                        }catch (NumberFormatException ignored){}

                        switch (moment.getPostType()) {
                            case "textPost":
                                //for posts with no video and images
                                findViewById(R.id.pd_postPicArea).setVisibility(View.GONE);
                                break;

                            case "imagePost":
                                //for posts with images
                                momentPic.setVisibility(View.VISIBLE);
                                try {
                                    Picasso.get().load(moment.getImageUrl()).into(momentPic);
                                } catch (NullPointerException ignored) {}
                                break;

                            case "sharedTextPost":
                                findViewById(R.id.pd_postPicArea).setVisibility(View.GONE);
                                sharedPost.setVisibility(View.VISIBLE);
                                getSharedMomentDetails(moment.getSharedPost());
                                break;

                            default:
                                Toast.makeText(MomentActivity.this, "Unknown Post Type Identified", Toast.LENGTH_SHORT).show();
                                break;
                        }

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
                                        } catch (NullPointerException ignored) {
                                        }
                                    }else{
                                        retailRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    Retail retail = ds.getValue(Retail.class);

                                                    if (retail.getRetail_id().equals(userID)){
                                                        postOwner = retail.getRetailName();
                                                        username.setText(postOwner);

                                                        try{
                                                            Picasso.get().load(retail.getImageUrl()).into(hisProPic);
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

    private void getSharedMomentDetails(String sharedPost) {
        momentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);
                    assert moment != null;
                    if (moment.getMomentId().equals(sharedPost)){
                        sharedPostDesc.setText(moment.getMoment_desc());

                        try{
                            sharedPostImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(moment.getImageUrl()).into(sharedPostImage);
                        }catch (NullPointerException ignored){}

                        try{
                            String timeTime = timeAgo.getTimeAgo(Long.parseLong(moment.getTimestamp()), MomentActivity.this);
                            sharedPostDate.setText(timeTime);
                        }catch (NumberFormatException ignored){}

                        try{
                            universalFunctions.findAddress(moment, sharedPostCheckIn, sharedLocationArea);
                        }catch (NullPointerException ignored){}

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dss : snapshot.getChildren()){
                                    User user = dss.getValue(User.class);

                                    if (user.getId().equals(moment.getUsername())){
                                        sharedPostUsername.setText(user.getUsername());

                                        try{
                                            Picasso.get().load(user.getImageUrl()).into(sharedPostProPic);
                                        }catch (NullPointerException ignored){}

                                    }else{
                                        retailRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    Retail retail = ds.getValue(Retail.class);

                                                    if (retail.getRetail_id().equals(moment.getUsername())){
                                                        sharedPostUsername.setText(retail.getRetailName());

                                                        try{
                                                            Picasso.get().load(retail.getImageUrl()).into(sharedPostProPic);
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(momentId);

        reference.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                try{
                    Picasso.get().load(user.getImageUrl()).into(my_image_profile);
                }catch(NullPointerException ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(momentId);
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
                if(!authorId.equals(firebaseUser.getUid())){
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
        hashMap.put("postid", momentId);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        reference.child(key).setValue(hashMap);
    }

}