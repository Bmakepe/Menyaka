package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.menyaka.Adapters.WishListAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Product;
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

public class WishListActivity extends AppCompatActivity {

    RecyclerView wishListRecycler, adsRecycler;
    TextView header;

    ArrayList<String> wished;
    ArrayList<Product> wishList;
    WishListAdapter wishListAdapter;

    DatabaseReference adsRef, wishListRef;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    LinearLayoutManager layoutManager1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_category);
        //setContentView(R.layout.recycler_view_layout);

        wishListRecycler = findViewById(R.id.categoryRecycler);
        header = findViewById(R.id.categoryHeader);
        adsRecycler = findViewById(R.id.addsRecycler);

        header.setText("Wish List");

        wished = new ArrayList<>();
        wishList = new ArrayList<>();
        adsList = new ArrayList<>();

        wishListRecycler.setHasFixedSize(true);
        wishListRecycler.setLayoutManager(new LinearLayoutManager(WishListActivity.this));

        wishListRef = FirebaseDatabase.getInstance().getReference("WishList");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");

        setAdsRV();
        getWishlist();
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

    private void getWishlist() {
        wishListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wished.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    wished.add(ds.getKey());
                }
                final DatabaseReference proRef = FirebaseDatabase.getInstance().getReference("Products");
                proRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        wishList.clear();
                        for(DataSnapshot snap : snapshot.getChildren()){
                            Product product = snap.getValue(Product.class);

                            for(String ID : wished){
                                if(product.getProductId().equals(ID)){
                                    wishList.add(product);
                                }
                            }
                        }
                        wishListAdapter = new WishListAdapter(WishListActivity.this, wishList);
                        wishListRecycler.setAdapter(wishListAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                adsAdapter = new BannerAdsAdapter(WishListActivity.this, adsList);
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