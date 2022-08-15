package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.menyaka.Adapters.SubCategoryAdapter;
import com.example.menyaka.Adapters.SubTabAdapter;
import com.example.menyaka.Adapters.TabAdapter;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class SubCategoriesActivity extends AppCompatActivity {

    /*String thisCategory, storeID;
    TextView header;
    ImageView backBTN;
    RecyclerView subCategoryRecycler;

    DatabaseReference subCatRef;
    ArrayList<String> subCategories;

    SubCategoryAdapter adapter;*/

    ArrayList<String> subCategories;

    TabLayout subCategoriesTabs;
    ViewPager viewPager;
    SubTabAdapter adapter;

    String thisCategory, storeID;
    TextView header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_categories_activity);

        Intent intent = getIntent();
        thisCategory = intent.getExtras().getString("storeCategory");
        storeID = intent.getExtras().getString("storeID");

        subCategoriesTabs = findViewById(R.id.shopSubCategoryTabs);
        viewPager = findViewById(R.id.subCategoriesPager);
        header = findViewById(R.id.subCatHeader);

        getStoreDetails();
        getSubCategories();

        findViewById(R.id.subCategoriesBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*header = findViewById(R.id.recyclerHeader);
        backBTN = findViewById(R.id.recyclerBackBTN);
        subCategoryRecycler = findViewById(R.id.recycler_View);

        Intent intent = getIntent();
        thisCategory = intent.getExtras().getString("storeCategory");
        storeID = intent.getExtras().getString("storeID");

        header.setText(thisCategory);
        subCategories = new ArrayList<>();

        subCategoryRecycler.hasFixedSize();
        subCategoryRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new GridLayoutManager(SubCategoriesActivity.this,2);
        subCategoryRecycler.setLayoutManager(layoutManager1);

        subCatRef = FirebaseDatabase.getInstance().getReference("Products");
        subCatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);

                    assert product != null;
                    if (product.getProductCategory().equals(thisCategory)){
                        if (product.getStoreId().equals(storeID)){
                            if (!subCategories.contains(product.getProductDepartment()))
                                subCategories.add(product.getProductDepartment());
                        }
                    }
                }

                adapter = new SubCategoryAdapter(SubCategoriesActivity.this, subCategories, storeID);
                subCategoryRecycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

    }

    private void getStoreDetails() {
        final DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    if (retail.getRetail_id().equals(storeID)){
                        header.setText(retail.getRetailName() +" " + thisCategory);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSubCategories() {
        subCategories = new ArrayList<>();
        final DatabaseReference subCatRef = FirebaseDatabase.getInstance().getReference("Products");
        subCatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subCategories.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);

                    assert product != null;
                    if (product.getProductCategory().equals(thisCategory)){
                        if (product.getStoreId().equals(storeID)){
                            if (!subCategories.contains(product.getProductDepartment()))
                                subCategories.add(product.getProductDepartment());
                        }
                    }
                }

                Collections.sort(subCategories);

                for (int i = 0; i < subCategories.size(); i++){
                    subCategoriesTabs.addTab(subCategoriesTabs.newTab().setText(subCategories.get(i)));
                }

                try{
                    adapter = new SubTabAdapter(getSupportFragmentManager(), subCategoriesTabs.getTabCount(), subCategories, SubCategoriesActivity.this, storeID);

                    viewPager.setAdapter(adapter);
                    viewPager.setOffscreenPageLimit(1);
                    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(subCategoriesTabs));

                    subCategoriesTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                }catch (IllegalArgumentException ignored){}

                //adapter = new SubCategoryAdapter(SubCategoriesActivity.this, subCategories, storeID);
                //subCategoryRecycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}