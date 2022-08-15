package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.ProfMediaAdapter;
import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.UserAdapter;
import com.example.menyaka.Adapters.ViewPagerAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.User;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.fragments.MyPostsFragment;
import com.example.menyaka.fragments.MyVideosFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView backBTN, verifiedUser;
    CircleImageView profilePic;
    TextView profileName, followersNo, followingNo, postsCounter, biography_TV, myProfileLocation, storesCounter;

    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private ProfMediaAdapter mediaPopAdapter;

    String image;
    double longitude, latitude;

    Dialog savedPostsDialog;

    TabLayout profileTabs;
    ViewPager profileViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profilePreferences, new ProfileFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        backBTN = findViewById(R.id.silhouetteBackBTN);
        profilePic = findViewById(R.id.profilePicture);
        profileName = findViewById(R.id.profileName);
        followersNo = findViewById(R.id.proFollowers);
        followingNo = findViewById(R.id.proFollowing);
        postsCounter = findViewById(R.id.postsCounter);
        biography_TV = findViewById(R.id.biography_TV);
        verifiedUser = findViewById(R.id.verifiedUser);
        myProfileLocation = findViewById(R.id.myProfileLocation);
        storesCounter = findViewById(R.id.proStoresCounter);
        profileTabs = findViewById(R.id.profileTabLayout);
        profileViewPager = findViewById(R.id.profileViewPager);

        //initiate firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new MyPostsFragment(),"Posts");
        viewPagerAdapter.addFragment(new MyVideosFragment(),"Videos");
        profileViewPager.setAdapter(viewPagerAdapter);
        profileTabs.setupWithViewPager(profileViewPager);

        getNrPosts();
        getStoresNo();
        loadMyDetails();
        getFollowersNo();
        iniSavedPostsDialog();

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ProfileActivity.this, InteractionsActivity.class);
                intent.putExtra("Interaction", "Followers");
                intent.putExtra("postID", firebaseUser.getUid());
                startActivity(intent);
            }
        });

        findViewById(R.id.following).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ProfileActivity.this, InteractionsActivity.class);
                intent.putExtra("Interaction", "Following");
                intent.putExtra("postID", firebaseUser.getUid());
                startActivity(intent);
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, FullScreenImageActivity.class);
                intent.putExtra("pictureID", firebaseUser.getUid());
                startActivity(intent);
            }
        });

        findViewById(R.id.profileMenuBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, findViewById(R.id.profileMenuBTN), Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0,0,"Saved Posts");
                popupMenu.getMenu().add(Menu.NONE, 1,0,"Settings");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if(id == 0){
                            savedPostsDialog.show();
                        }else if (id == 1){
                            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        findViewById(R.id.viewMoreLoyalty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, LoyaltyActivity.class));
            }
        });

        findViewById(R.id.viewMoreOrders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, MyOrdersActivity.class));
            }
        });

        findViewById(R.id.stores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, MyStoresActivity.class));
            }
        });

        myProfileLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = "https://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                startActivity(intent);

            }
        });
    }

    private void getStoresNo() {
        final DatabaseReference storesRef = FirebaseDatabase.getInstance().getReference("MyStores");
        storesRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                storesCounter.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void iniSavedPostsDialog() {
        savedPostsDialog = new Dialog(this);
        savedPostsDialog.setContentView(R.layout.recycler_view_layout);
        savedPostsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        savedPostsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        savedPostsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        RecyclerView savedRecycler = savedPostsDialog.findViewById(R.id.recycler_View);
        TextView header = savedPostsDialog.findViewById(R.id.recyclerHeader);

        savedRecycler.setVisibility(View.VISIBLE);
        header.setText("Saved Posts");

        List<String> postSaves = new ArrayList<>();
        List<Moment> postListSaves = new ArrayList<>();

        savedRecycler.hasFixedSize();
        savedRecycler.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));

        PostAdapter postAdapter = new PostAdapter(postListSaves, ProfileActivity.this);
        savedRecycler.setAdapter(postAdapter);

        final DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid());
        savedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postSaves.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    postSaves.add(ds.getKey());

                    final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Moments");
                    postRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            postListSaves.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                Moment moment = ds.getValue(Moment.class);

                                for (String ID : postSaves){
                                    if (moment.getMomentId().equals(ID)){
                                        postListSaves.add(moment);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        savedPostsDialog.findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savedPostsDialog.dismiss();
            }
        });
    }

    private void loadMyDetails() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                image = user.getImageUrl();

                profileName.setText(user.getUsername());
                biography_TV.setText(user.getBiography());

                if (user.isVerified())
                    verifiedUser.setVisibility(View.VISIBLE);
                else
                    verifiedUser.setVisibility(View.GONE);

                try{
                    latitude = user.getLatitude();
                    longitude = user.getLongitude();

                    findAddress();

                    Picasso.get().load(user.getImageUrl()).into(profilePic);
                }catch (NullPointerException ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            myProfileLocation.setText(address);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                    if (post.getUsername().equals(firebaseUser.getUid())){
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

    private void getFollowersNo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of followers

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of user i follow
    }

    public static class ProfileFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.profile_preferences, rootKey);

        }
    }
}