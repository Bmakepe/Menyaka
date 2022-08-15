package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.ProductListAdapter;
import com.example.menyaka.Models.ChatList;
import com.example.menyaka.Models.Product;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoryStockActivity extends AppCompatActivity {

    String thisDepartment, storeID;
    TextView header;
    ImageView backBTN;
    RecyclerView productRecycler;
    ProductListAdapter adapter;
    ArrayList<Product> products;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        header = findViewById(R.id.recyclerHeader);
        backBTN = findViewById(R.id.recyclerBackBTN);
        productRecycler = findViewById(R.id.recycler_View);

        Intent intent = getIntent();
        thisDepartment = intent.getExtras().getString("productDepartment");
        storeID = intent.getExtras().getString("storeID");

        header.setText(thisDepartment);

        products = new ArrayList<>();

        productRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new GridLayoutManager(CategoryStockActivity.this,2);
        productRecycler.setLayoutManager(layoutManager1);
        adapter = new ProductListAdapter(CategoryStockActivity.this, products);
        productRecycler.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference("Products");

        getCategoryProducts();

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getCategoryProducts() {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    assert product != null;
                    if(product.getProductDepartment().equals(thisDepartment)) {
                        if (product.getStoreId().equals(storeID))
                            products.add(product);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}