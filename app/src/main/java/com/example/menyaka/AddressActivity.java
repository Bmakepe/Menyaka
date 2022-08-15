package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.menyaka.Adapters.AddressAdapter;
import com.example.menyaka.Models.Address;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity {

    //for retrieving saved addresses
    ArrayList<Address> addresses;
    RecyclerView addressRecycler;
    AddressAdapter addressAdapter;

    FirebaseUser firebaseUser;
    DatabaseReference addressRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_address);

        addressRecycler = findViewById(R.id.addressRecycler);
        addressRecycler.setHasFixedSize(true);
        addressRecycler.setLayoutManager(new LinearLayoutManager(AddressActivity.this));

        addresses = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        addressRef = FirebaseDatabase.getInstance().getReference("DeliveryAddress");

        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                addresses.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Address address = ds.getValue(Address.class);
                    assert address != null;
                    if(address.getUserID().equals(firebaseUser.getUid()))
                        addresses.add(address);
                }

                addressAdapter = new AddressAdapter(AddressActivity.this, addresses);
                addressRecycler.setAdapter(addressAdapter);

                addressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        findViewById(R.id.add_new_address_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddressActivity.this, NewAddressActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.addressBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}