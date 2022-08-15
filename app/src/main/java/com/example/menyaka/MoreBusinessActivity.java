package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Retail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MoreBusinessActivity extends AppCompatActivity {

    private RetailAdapter businessAdapter;
    ArrayList<MyStore> retailList;
    RecyclerView businessRecycler;
    TextView header;
    String storeCategory, storeId, style;
    DatabaseReference storeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        header = findViewById(R.id.recyclerHeader);
        header.setText("Similar Businesses");
        businessRecycler = findViewById(R.id.recycler_View);

        Intent intent = getIntent();
        storeId = intent.getExtras().getString("storeId");
        storeCategory = intent.getExtras().getString("storeCategory");
        style = intent.getExtras().getString("style");

        retailList = new ArrayList<>();

        storeRef = FirebaseDatabase.getInstance().getReference("Retails");

        businessRecycler.setHasFixedSize(true);
        businessRecycler.setLayoutManager(new GridLayoutManager(MoreBusinessActivity.this, 3));

        if (style.equals("similarProductStores")){
            readSimilarProductStores();
        }else if (style.equals("similarStores")){
            readSimilarBusinesses();
        }

        businessAdapter = new RetailAdapter(MoreBusinessActivity.this, retailList);
        businessRecycler.setAdapter(businessAdapter);

        businessAdapter.notifyDataSetChanged();

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void readSimilarProductStores() {
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                retailList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    MyStore retail = ds.getValue(MyStore.class);

                    retailList.add(retail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readSimilarBusinesses(){
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                retailList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MyStore retail = dataSnapshot.getValue(MyStore.class);
                    assert retail != null;
                    if(!retail.getRetail_id().equals(storeId))
                        if(retail.getCategory().equals(storeCategory))
                            retailList.add(retail);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}