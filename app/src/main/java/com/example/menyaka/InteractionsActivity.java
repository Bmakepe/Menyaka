package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.UserAdapter;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InteractionsActivity extends AppCompatActivity {

    TextView header;
    ImageView backBTN;
    RecyclerView listRecycler;

    String interaction, postID;

    ArrayList<User> userList;
    ArrayList<String> idList;

    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        header = findViewById(R.id.recyclerHeader);
        backBTN = findViewById(R.id.recyclerBackBTN);
        listRecycler = findViewById(R.id.recycler_View);

        Intent intent = getIntent();
        interaction = intent.getStringExtra("Interaction");
        postID = intent.getStringExtra("postID");

        header.setText(interaction);

        userList = new ArrayList<>();
        idList = new ArrayList<>();

        listRecycler.hasFixedSize();
        listRecycler.setHasFixedSize(true);
        listRecycler.setLayoutManager(new LinearLayoutManager(this));

        switch (interaction){
            case "Likes":
                getLikes();
                break;

            case "Views":
                getViews();
                break;

            case "Followers":
                getFollowers();
                break;

            case "Following":
                getFollowing();
                break;

            default:
                Toast.makeText(this, "Unable to resolve request", Toast.LENGTH_SHORT).show();
                finish();
        }

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getFollowing() {
        final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(postID).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            for (String id : idList){
                                if (user.getId().equals(id)){
                                    userList.add(user);
                                }
                            }
                            userAdapter = new UserAdapter(InteractionsActivity.this, userList);
                            listRecycler.setAdapter(userAdapter);
                            Collections.sort(userList, (contactsModel, t1) -> contactsModel.getUsername().compareTo(t1.getUsername()));
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

    private void getFollowers() {
        final DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(postID).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot data : snapshot.getChildren()){
                            User user = data.getValue(User.class);

                            for (String ID : idList){
                                if (user.getId().equals(ID)){
                                    userList.add(user);
                                }
                            }
                            userAdapter = new UserAdapter(InteractionsActivity.this, userList);
                            listRecycler.setAdapter(userAdapter);

                            Collections.sort(userList, new Comparator<User>() {
                                @Override
                                public int compare(User contactsModel, User t1) {
                                    return contactsModel.getUsername().compareTo(t1.getUsername());
                                }
                            });
                        }
                        userAdapter.notifyDataSetChanged();
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

    private void getViews() {
        final DatabaseReference viewsRef = FirebaseDatabase.getInstance().getReference("MenyakaViews").child(postID);
        viewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());

                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for(DataSnapshot data : snapshot.getChildren()){
                                User user = data.getValue(User.class);
                                for(String ID : idList){
                                    if (user.getId().equals(ID)){
                                        userList.add(user);
                                    }
                                }
                            }
                            userAdapter =  new UserAdapter(InteractionsActivity.this, userList);
                            listRecycler.setAdapter(userAdapter);
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
    }

    private void getLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postID);
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());

                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for (DataSnapshot data : snapshot.getChildren()){
                                User user = data.getValue(User.class);
                                for(String ID : idList){
                                    if (user.getId().equals(ID)){
                                        userList.add(user);
                                    }
                                }
                            }
                            userAdapter =  new UserAdapter(InteractionsActivity.this, userList);
                            listRecycler.setAdapter(userAdapter);
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
    }
}