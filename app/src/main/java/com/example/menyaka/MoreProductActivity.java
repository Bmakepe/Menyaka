package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.ProductListAdapter;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MoreProductActivity extends AppCompatActivity {

    ImageView back;
    TextView storeName;
    String storeId, style;
    private ProductListAdapter productAdapter;
    private RecyclerView productsRecycler;
    private ArrayList<Product> productList;

    DatabaseReference storeRef, productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        productsRecycler = findViewById(R.id.recycler_View);
        back = findViewById(R.id.recyclerBackBTN);
        storeName = findViewById(R.id.recyclerHeader);

        Intent intent = getIntent();
        storeId = intent.getExtras().getString("storeId");
        style = intent.getExtras().getString("style");

        productsRecycler.setHasFixedSize(true);
        productsRecycler.setLayoutManager(new GridLayoutManager(MoreProductActivity.this, 2));
        productList = new ArrayList<>();

        productsRef = FirebaseDatabase.getInstance().getReference("Products");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");

        getStoreDetails();

        if (style.equals("similarProducts")){
            loadSimilarProducts();
        }else if (style.equals("businessProducts")){
            loadStoreProducts();
        }

        productAdapter = new ProductListAdapter(MoreProductActivity.this, productList);
        productsRecycler.setAdapter(productAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void loadSimilarProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    productList.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoreDetails() {
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    assert retail != null;
                    if(retail.getRetail_id().equals(storeId))
                        storeName.setText(retail.getRetailName() +" Products");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadStoreProducts() {
        Query query = productsRef.orderByChild("storeId").equalTo(storeId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    productList.add(product);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*private void readProductCategory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Product product = snapshot.getValue(Product.class);
                    if(product.getStoreId().equals(storeId))
                        productList.add(product);
                }

                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}