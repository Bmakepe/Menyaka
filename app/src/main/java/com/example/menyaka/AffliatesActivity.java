package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.menyaka.Adapters.AffiliatesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AffliatesActivity extends AppCompatActivity {

    TextView header;
    RecyclerView contactsRecycler;

    ArrayList<String> myAffliates;

    DatabaseReference myStoresRef;
    FirebaseUser firebaseUser;

    AffiliatesAdapter affiliatesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        contactsRecycler = findViewById(R.id.recycler_View);
        header = findViewById(R.id.recyclerHeader);

        header.setText("Contacts");

        myAffliates = new ArrayList<>();
        contactsRecycler.hasFixedSize();
        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myStoresRef = FirebaseDatabase.getInstance().getReference("MyStores");
        getFollowingList();

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getFollowingList() {
        final DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");
        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAffliates.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    myAffliates.add(ds.getKey());//this will add users and agents i am following

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myStoresRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    myAffliates.add(ds.getKey()); //this will add my fav stores to the list
                }

                affiliatesAdapter = new AffiliatesAdapter(AffliatesActivity.this, myAffliates);
                contactsRecycler.setAdapter(affiliatesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}