package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.ProductListAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProductListActivity extends AppCompatActivity {
    
    TextView productsHeader;
    
    RecyclerView productsRecycler, adsRecycler;
    ProductListAdapter productAdapter;
    ArrayList<Product> productList;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    DatabaseReference productRef, adsRef;

    LinearLayoutManager layoutManager1;

    //ImageSlider advertHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_category);

        productsHeader = findViewById(R.id.categoryHeader);
        productsRecycler = findViewById(R.id.categoryRecycler);
        adsRecycler = findViewById(R.id.addsRecycler);
        //advertHeader = findViewById(R.id.advertHeader);

        productsHeader.setText("Products");

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");

        productsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new GridLayoutManager(this, 2);
        productsRecycler.setLayoutManager(layoutManager);

        productList = new ArrayList<>();
        adsList = new ArrayList<>();

        setAdsRV();
        getProductList();
        iniAdBanners();

        findViewById(R.id.categoryBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void setAdsRV() {

        adsRecycler.hasFixedSize();
        layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        adsRecycler.setLayoutManager(layoutManager1);


    }

    private void getProductList() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    productList.add(product);
                }

                Collections.shuffle(productList);
                productAdapter = new ProductListAdapter(ProductListActivity.this, productList);
                productsRecycler.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void iniAdBanners() {

        adsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    BannerAds ads = ds.getValue(BannerAds.class);

                    adsList.add(ads);

                }
                Collections.shuffle(adsList);
                adsAdapter = new BannerAdsAdapter(ProductListActivity.this, adsList);
                adsRecycler.setAdapter(adsAdapter);

                LinearSnapHelper snapHelper = new LinearSnapHelper();
                snapHelper.attachToRecyclerView(adsRecycler);

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (layoutManager1.findLastCompletelyVisibleItemPosition() < (adsAdapter.getItemCount() -1)){
                            layoutManager1.smoothScrollToPosition(adsRecycler, new RecyclerView.State(), layoutManager1.findLastCompletelyVisibleItemPosition() + 1);
                        }else if (layoutManager1.findLastCompletelyVisibleItemPosition() < (adsAdapter.getItemCount() -1)){
                            layoutManager1.smoothScrollToPosition(adsRecycler, new RecyclerView.State(), 0);
                        }
                    }
                }, 0, 10000);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}