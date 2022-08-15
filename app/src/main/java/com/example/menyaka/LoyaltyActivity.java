package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.LoyaltyAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Points;
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

public class LoyaltyActivity extends AppCompatActivity {

    ImageView backBTN;
    TextView header;
    RecyclerView pointsRecycler, adsRecycler;

    DatabaseReference pointsRef, adsRef;
    FirebaseUser firebaseUser;

    ArrayList<Points> points;
    LoyaltyAdapter loyaltyAdapter;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    LinearLayoutManager layoutManager1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_category);

        backBTN = findViewById(R.id.categoryBackBTN);
        header = findViewById(R.id.categoryHeader);
        pointsRecycler = findViewById(R.id.categoryRecycler);
        adsRecycler = findViewById(R.id.addsRecycler);

        header.setText("Menyaka Loyalty Points");

        pointsRecycler.hasFixedSize();
        pointsRecycler.setLayoutManager(new LinearLayoutManager(this));

        points = new ArrayList<>();
        adsList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        pointsRef = FirebaseDatabase.getInstance().getReference("MenyakaPoints").child(firebaseUser.getUid());
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");

        setAdsRV();
        getPointsInfo();
        iniAdBanners();

        backBTN.setOnClickListener(new View.OnClickListener() {
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

    private void getPointsInfo() {
        pointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    points.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Points myPoints = ds.getValue(Points.class);

                        assert myPoints != null;
                        points.add(myPoints);
                    }
                }else{
                    Toast.makeText(LoyaltyActivity.this, "You have not enrolled in any programs", Toast.LENGTH_SHORT).show();
                }

                loyaltyAdapter = new LoyaltyAdapter(LoyaltyActivity.this, points);
                pointsRecycler.setAdapter(loyaltyAdapter);
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
                adsAdapter = new BannerAdsAdapter(LoyaltyActivity.this, adsList);
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