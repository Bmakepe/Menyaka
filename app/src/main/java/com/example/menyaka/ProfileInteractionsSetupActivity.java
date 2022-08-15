package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.StoreInteractionAdapter;
import com.example.menyaka.Adapters.UserInteractionAdapter;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ProfileInteractionsSetupActivity extends AppCompatActivity {

    TextView moreStores, moreUsers;
    RecyclerView storesRecycler, usersRecycler;

    ArrayList<Retail> retailList;
    ArrayList<User> userList;

    DatabaseReference userRef, retailRef;
    FirebaseUser firebaseUser;

    StoreInteractionAdapter storeInteractionAdapter;
    UserInteractionAdapter userInteractionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_interactions_setup);

        moreStores = findViewById(R.id.proSetup_moreShops);
        moreUsers = findViewById(R.id.proSetup_seeMoreBTN);
        storesRecycler = findViewById(R.id.proSetup_storesRecycler);
        usersRecycler = findViewById(R.id.proSetup_usersRecycler);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        retailList = new ArrayList<>();
        storesRecycler.hasFixedSize();
        storesRecycler.setLayoutManager(new GridLayoutManager(ProfileInteractionsSetupActivity.this, 3));
        //storesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        userList = new ArrayList<>();
        usersRecycler.hasFixedSize();
        usersRecycler.setLayoutManager(new GridLayoutManager(ProfileInteractionsSetupActivity.this, 3));

        getStores();
        getUsers();

        moreStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileInteractionsSetupActivity.this, "You will be able to see all the registered stores", Toast.LENGTH_SHORT).show();
            }
        });

        moreUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileInteractionsSetupActivity.this, "You will be able to see all the registered users", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.profileBuildBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.goToTimeline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileInteractionsSetupActivity.this, MainActivity.class));
            }
        });

    }

    private void getUsers() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (!user.getId().equals(firebaseUser.getUid()))
                        userList.add(user);
                }
                Collections.shuffle(userList);
                userInteractionAdapter = new UserInteractionAdapter(userList, ProfileInteractionsSetupActivity.this);
                usersRecycler.setAdapter(userInteractionAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStores() {
        retailRef.limitToFirst(6).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                retailList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    retailList.add(retail);
                }
                Collections.shuffle(retailList);
                storeInteractionAdapter = new StoreInteractionAdapter(retailList, ProfileInteractionsSetupActivity.this);
                storesRecycler.setAdapter(storeInteractionAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}