package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.ShopCategoryAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ShopCategoryActivity extends AppCompatActivity {

    ImageView back;
    TextView storeName;
    String storeId;
    RecyclerView categoryRecycler, adsRecycler;
    ShopCategoryAdapter adapter;
    ArrayList<Product> productArrayList;
    ArrayList<String> storeCategory, storeID;

    DatabaseReference catRef, storeRef, adsRef;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    LinearLayoutManager layoutManager1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_category);

        Intent intent = getIntent();
        storeId = intent.getExtras().getString("storeId");

        back = findViewById(R.id.categoryBackBTN);
        storeName = findViewById(R.id.categoryHeader);
        categoryRecycler = findViewById(R.id.categoryRecycler);
        adsRecycler = findViewById(R.id.addsRecycler);

        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");
        catRef = FirebaseDatabase.getInstance().getReference("Products");

        productArrayList = new ArrayList<>();
        storeCategory = new ArrayList<>();
        storeID = new ArrayList<>();
        adsList = new ArrayList<>();

        categoryRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new GridLayoutManager(ShopCategoryActivity.this,2);
        categoryRecycler.setLayoutManager(layoutManager1);

        setAdsRV();
        getCategoryDetails();
        getStoreDetails();
        iniAdBanners();

        back.setOnClickListener(new View.OnClickListener() {
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

    private void getCategoryDetails() {
        catRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productArrayList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    assert product != null;
                    if(product.getStoreId().equals(storeId)){
                        if(!storeCategory.contains(product.getProductCategory())) {
                            storeCategory.add(product.getProductCategory());
                            storeID.add(product.getStoreId());
                        }
                    }
                }

                adapter = new ShopCategoryAdapter(ShopCategoryActivity.this, storeID, storeCategory);
                categoryRecycler.setAdapter(adapter);
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
                for(DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    assert retail != null;
                    if(retail.getRetail_id().equals(storeId))
                        storeName.setText(retail.getRetailName() +" Categories");
                }
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
                adsAdapter = new BannerAdsAdapter(ShopCategoryActivity.this, adsList);
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
                }, 0, 3000);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}