package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.HisTabAdapter;
import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.UserAdapter;
import com.example.menyaka.Adapters.ViewPagerAdapter;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.User;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.fragments.HisPostsFragment;
import com.example.menyaka.fragments.HisVideosFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    //for user profile details
    CircleImageView profilePic;
    TextView hisUsername, biography_TV, postsCounter, followersCounter, followingCounter;
    ImageView profileMenu, hisVerification;

    String hisUserID, hisName;

    Button sendMessage;

    FirebaseUser firebaseUser;

    TabLayout hisTabLayout;
    ViewPager hisViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        profilePic = findViewById(R.id.viewProfilePicture);
        hisUsername = findViewById(R.id.viewProfileName);
        postsCounter = findViewById(R.id.hisPostsCounter);
        followersCounter = findViewById(R.id.hisFollowersCounter);
        followingCounter = findViewById(R.id.hisFollowingCounter);
        profileMenu = findViewById(R.id.viewProfileMenu);
        sendMessage = findViewById(R.id.sendMessageBTN);
        biography_TV = findViewById(R.id.his_biography_TV);
        hisVerification = findViewById(R.id.hisVerification);
        hisTabLayout = findViewById(R.id.hisProfileTabLayout);
        hisViewPager = findViewById(R.id.hisProfileViewPager);

        Intent intent = getIntent();
        hisUserID = intent.getStringExtra("userID");

        Bundle bundle = new Bundle();
        bundle.putString("hisUserID", hisUserID);

        HisPostsFragment postsFragment = new HisPostsFragment();
        postsFragment.setArguments(bundle);

        HisVideosFragment videosFragment = new HisVideosFragment();
        videosFragment.setArguments(bundle);

        getUserDetails();
        getNrPosts();
        getFollowData();
        checkFollowing();

        HisTabAdapter viewPagerAdapter = new HisTabAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HisPostsFragment(),"Posts", hisUserID);
        viewPagerAdapter.addFragment(new HisVideosFragment(),"Videos", hisUserID);
        hisViewPager.setAdapter(viewPagerAdapter);
        hisTabLayout.setupWithViewPager(hisViewPager);

        findViewById(R.id.viewProfileBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.viewProfileMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ViewProfileActivity.this, "You will view the profile menu details", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.hisFollowers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //followersDialog.show();

                Intent intent = new Intent(ViewProfileActivity.this, InteractionsActivity.class);
                intent.putExtra("Interaction", "Followers");
                intent.putExtra("postID", hisUserID);
                startActivity(intent);
            }
        });

        findViewById(R.id.hisFollowing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //followingDialog.show();

                Intent intent = new Intent(ViewProfileActivity.this, InteractionsActivity.class);
                intent.putExtra("Interaction", "Following");
                intent.putExtra("postID", hisUserID);
                startActivity(intent);
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sendMessage.getText().equals("Message")) {
                    Intent intent = new Intent(ViewProfileActivity.this, MessageActivity.class);
                    intent.putExtra("receiverID", hisUserID);
                    startActivity(intent);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(hisUserID).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(hisUserID)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications();
                }
            }
        });
        
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(ViewProfileActivity.this, profileMenu, Gravity.END);
                if(sendMessage.getText().equals("Message"))
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Unfollow " + hisName);

                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Turn On/Off Post Notifications");
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Block/Unblock " + hisName);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        switch (id){
                            case 0:

                                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                        .child("following").child(hisUserID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("Follow").child(hisUserID)
                                        .child("followers").child(firebaseUser.getUid()).removeValue();

                                break;

                            case 1:
                                Toast.makeText(ViewProfileActivity.this, "You will be able to turn on/off Post Notifications for " + hisName, Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                Toast.makeText(ViewProfileActivity.this, "You will be able to block or unblock " + hisName, Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                Toast.makeText(ViewProfileActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                intent.putExtra("pictureID", hisUserID);
                startActivity(intent);
            }
        });
    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(hisUserID);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ViewProfileActivity.this, "Following", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowing() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(hisUserID).exists()){
                    //executes if following user
                    sendMessage.setText("Message");
                }else{
                    sendMessage.setText("Follow");
                    //executes if you are not following the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisUserID).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followersCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of followers

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisUserID).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of user i follow
    }

    private void getNrPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Moments");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Moment post = snapshot.getValue(Moment.class);
                    assert post != null;
                    if (post.getUsername().equals(hisUserID)){
                        i++;
                    }
                }

                try{
                    postsCounter.setText(i+"");
                }catch (NullPointerException ignored){ }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserDetails() {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if(user.getId().equals(hisUserID)){
                        hisName = user.getUsername();
                        hisUsername.setText(user.getUsername());
                        biography_TV.setText(user.getBiography());

                        if (user.isVerified())
                            hisVerification.setVisibility(View.VISIBLE);
                        else
                            hisVerification.setVisibility(View.GONE);

                        try{
                            Picasso.get().load(user.getImageUrl()).into(profilePic);
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