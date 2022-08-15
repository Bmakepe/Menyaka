package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Retail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MyStoresActivity extends AppCompatActivity {

    TextView storesHeader;
    RecyclerView storesRecycler, adsRecycler;

    ArrayList<MyStore> shopList;
    ArrayList<String> myLikedStores;

    DatabaseReference myStoresRef, retailRef, adsRef;
    FirebaseUser firebaseUser;

    RetailAdapter retailAdapter;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    LinearLayoutManager layoutManager1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_category);

        storesHeader = findViewById(R.id.categoryHeader);
        storesRecycler = findViewById(R.id.categoryRecycler);
        adsRecycler = findViewById(R.id.addsRecycler);

        storesHeader.setText("My Stores");

        storesRecycler.hasFixedSize();
        storesRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new GridLayoutManager(this, 3);
        storesRecycler.setLayoutManager(layoutManager);

        shopList = new ArrayList<>();
        myLikedStores = new ArrayList<>();

        myStoresRef = FirebaseDatabase.getInstance().getReference("MyStores");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        setAdsRV();
        getMyStoresRefs();
        iniAdBanners();
        adsList = new ArrayList<>();

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

    private void getMyStoresRefs() {

        myStoresRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myLikedStores.clear();;
                shopList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    myLikedStores.add(ds.getKey());
                }

                retailRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot data : snapshot.getChildren()){
                            MyStore retail = data.getValue(MyStore.class);

                            for(String ID : myLikedStores){
                                assert retail != null;
                                if(retail.getRetail_id().equals(ID)){
                                    shopList.add(retail);
                                }
                            }
                        }
                        Collections.shuffle(shopList);

                        retailAdapter = new RetailAdapter(MyStoresActivity.this, shopList);
                        storesRecycler.setAdapter(retailAdapter);

                        //Collections.sort(shopList, (storesModel, t1) -> storesModel.getRetailName().compareTo(t1.getRetailName()));

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
                adsAdapter = new BannerAdsAdapter(MyStoresActivity.this, adsList);
                adsRecycler.setAdapter(adsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}