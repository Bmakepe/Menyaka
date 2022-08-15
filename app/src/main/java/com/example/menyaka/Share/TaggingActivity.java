package com.example.menyaka.Share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.FriendsTagAdapter;
import com.example.menyaka.Adapters.StoreTagAdapter;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.google.android.material.snackbar.Snackbar;
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

public class TaggingActivity extends AppCompatActivity {

    //private VideoView videoView;

    RecyclerView allItemsRecycler;
    TextView header;
    ImageView doneBTN;

    String headerText;

    FirebaseUser firebaseUser;

    //for retrieving tagged items
    ArrayList<User> taggedUsers;
    ArrayList<String> usersTagged;
    ArrayList<MyStore> taggedStores;
    ArrayList<String> storesTagged;

    //for retrieving stores list
    ArrayList<String> likedStores;
    ArrayList<MyStore> favStores;

    //for retrieving friends list
    List<String> allFollowing;
    List<User> followingList;

    //Adapters
    StoreTagAdapter retails;
    FriendsTagAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        allItemsRecycler = findViewById(R.id.recycler_View);
        header = findViewById(R.id.recyclerHeader);
        doneBTN = findViewById(R.id.recyclerDoneBTN);

        Intent intent = getIntent();
        headerText = intent.getStringExtra("headerText");
        doneBTN.setVisibility(View.VISIBLE);

        allItemsRecycler.setHasFixedSize(true);
        allItemsRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        allItemsRecycler.setLayoutManager(layoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //for retrieving stores list
        likedStores = new ArrayList<>();
        favStores = new ArrayList<>();

        //for retrieving friends list
        allFollowing = new ArrayList<>();
        followingList = new ArrayList<>();

        //for tagged
        taggedUsers = new ArrayList<>();
        usersTagged = new ArrayList<>();

        taggedStores = new ArrayList<>();
        storesTagged = new ArrayList<>();

        taggedUsers.clear();
        usersTagged.clear();

        taggedStores.clear();
        storesTagged.clear();

        if(headerText.equals("storeTags")){
            getStoresList();
            prepareStoreTags();
        }else if (headerText.equals("friendTags")){
            getFriendsList();
            prepareFriendTags();
        }else{
            Toast.makeText(this, "Unknown Selection", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        doneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachTags();
            }
        });

    }

    private void prepareFriendTags() {

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {

                    int position = viewHolder.getAdapterPosition();
                    User myFriend = followingList.get(viewHolder.getAdapterPosition());
                    followingList.remove(position);
                    //taggedUsers.add(myFriend);
                    usersTagged.add(myFriend.getId());
                    userAdapter.notifyItemRemoved(position);

                    Toast.makeText(TaggingActivity.this, "you will be able to tag " + myFriend.getUsername() + ". The number of items tagged is " + usersTagged.size(), Toast.LENGTH_SHORT).show();

                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(allItemsRecycler);
    }

    private void prepareStoreTags() {

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {

                    int position = viewHolder.getAdapterPosition();
                    MyStore myStore = favStores.get(viewHolder.getAdapterPosition());
                    favStores.remove(position);
                    taggedStores.add(myStore);
                    retails.notifyItemRemoved(position);

                    Toast.makeText(TaggingActivity.this, "you will be able to tag "+ myStore.getRetailName() +". The number of items tagged is " + taggedStores.size(), Toast.LENGTH_SHORT).show();

                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(allItemsRecycler);
    }

    private void attachTags() {
        if(headerText.equals("storeTags")){

            Toast.makeText(this, "Thank you for the tags. You will be able to tag " + taggedStores.size() + " stores.",Toast.LENGTH_SHORT).show();

        }else if (headerText.equals("friendTags")){

            Intent friendsIntent = new Intent();
            friendsIntent.putStringArrayListExtra("taggedFriends", usersTagged);

            Toast.makeText(this, "Thank you for the tags. You will be able to tag " + usersTagged.size() + " friends.",Toast.LENGTH_SHORT).show();

            finish();
        }

    }

    private void getFriendsList() {
        header.setText("Tag Your Friends");

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFollowing.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    allFollowing.add(ds.getKey());
                }

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followingList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user = dataSnapshot.getValue(User.class);

                            for(String id : allFollowing){
                                assert user != null;
                                if(user.getId().equals(id))
                                    followingList.add(user);

                            }

                            userAdapter = new FriendsTagAdapter(TaggingActivity.this, followingList);
                            allItemsRecycler.setAdapter(userAdapter);

                            Collections.sort(followingList, (contactsModel, t1) -> contactsModel.getUsername().compareTo(t1.getUsername()));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoresList() {
        header.setText("Tag Your Favourite Stores");

        allItemsRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(TaggingActivity.this);
        allItemsRecycler.setLayoutManager(layoutManager1);

        final DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("MyStores").child(firebaseUser.getUid());
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likedStores.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    likedStores.add(ds.getKey());
                }
                final DatabaseReference RetailRef = FirebaseDatabase.getInstance().getReference("Retails");
                RetailRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favStores.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MyStore retail = dataSnapshot.getValue(MyStore.class);
                            for(String id : likedStores) {
                                assert retail != null;
                                if(retail.getRetail_id().equals(id))
                                    favStores.add(retail);
                            }
                        }

                        retails = new StoreTagAdapter(TaggingActivity.this, favStores);
                        allItemsRecycler.setAdapter(retails);

                        Collections.sort(favStores, (storesModel, t1) -> storesModel.getRetailName().compareTo(t1.getRetailName()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}