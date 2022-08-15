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

import com.example.menyaka.Adapters.HotDealAdapter;
import com.example.menyaka.Adapters.HotDealListAdapter;
import com.example.menyaka.Models.HotDeal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HotDealActivity extends AppCompatActivity {

    TextView hotDealsHeader;

    RecyclerView hotDealsRecycler;

    List<String> id_list;

    List<HotDeal> hotDealsList;

    DatabaseReference hotDealsReference;
    private HotDealListAdapter hotDealAdapter;

    private String storeID, listStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        listStyle = getIntent().getExtras().getString("listStyle");

        hotDealsHeader = findViewById(R.id.recyclerHeader);
        hotDealsRecycler = findViewById(R.id.recycler_View);

        hotDealsHeader.setText("Hot Deals");

        hotDealsReference = FirebaseDatabase.getInstance().getReference("HotDeals");

        hotDealsRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new GridLayoutManager(this, 3);
        hotDealsRecycler.setLayoutManager(layoutManager1);

        hotDealsList = new ArrayList<>();
        id_list = new ArrayList<>();

        if (listStyle.equals("filteredList")){
            storeID = getIntent().getExtras().getString("storeID");
            loadStoreHotDeals();
        }else{
            loadHotDeals();
        }

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadStoreHotDeals() {
        hotDealsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotDealsList.clear();
                id_list.clear();
                for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                    id_list.add(dataSnapshot.getKey());

                    DatabaseReference hotDeal_list = FirebaseDatabase.getInstance().getReference().child("HotDeals").child(Objects.requireNonNull(dataSnapshot.getKey()));
                    hotDeal_list.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                                HotDeal hotDeal = dataSnapshot.getValue(HotDeal.class);
                                if (hotDeal.getStoreId().equals(storeID))
                                    hotDealsList.add(hotDeal);
                            }
                            hotDealAdapter = new HotDealListAdapter(hotDealsList,HotDealActivity.this);
                            hotDealsRecycler.setAdapter(hotDealAdapter);
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

    private void loadHotDeals() {
        hotDealsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotDealsList.clear();
                id_list.clear();
                for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                    id_list.add(dataSnapshot.getKey());

                    DatabaseReference hotDeal_list = FirebaseDatabase.getInstance().getReference().child("HotDeals").child(Objects.requireNonNull(dataSnapshot.getKey()));
                    hotDeal_list.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                                HotDeal hotDeal = dataSnapshot.getValue(HotDeal.class);
                                hotDealsList.add(hotDeal);
                            }
                            hotDealAdapter = new HotDealListAdapter(hotDealsList,HotDealActivity.this);
                            hotDealsRecycler.setAdapter(hotDealAdapter);
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