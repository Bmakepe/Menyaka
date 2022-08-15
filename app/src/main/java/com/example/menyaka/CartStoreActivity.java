package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.CartStoreAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.CartItems;
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

public class CartStoreActivity extends AppCompatActivity {

    TextView header;
    ImageView backBTN;

    RecyclerView cartStoresRecycler, adsRecycler;
    CartStoreAdapter cartStoreAdapter;

    ArrayList<Retail> retails;
    ArrayList<CartItems> products;
    ArrayList<String> myCartStoreIDs;
    ArrayList<String> cartKeys;

    DatabaseReference retailRef, cartRef, adsRef;
    FirebaseUser user;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    SearchView searchView;

    LinearLayoutManager layoutManager1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_category);

        header = findViewById(R.id.categoryHeader);
        backBTN = findViewById(R.id.categoryBackBTN);
        cartStoresRecycler = findViewById(R.id.categoryRecycler);
        adsRecycler = findViewById(R.id.addsRecycler);
        searchView = findViewById(R.id.catSearchView);

        header.setText("Stores I Have Bought From");

        myCartStoreIDs = new ArrayList<>();
        retails = new ArrayList<>();
        products = new ArrayList<>();
        cartKeys = new ArrayList<>();
        adsList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        cartRef = FirebaseDatabase.getInstance().getReference("ShoppingCarts");
        //cartRef = FirebaseDatabase.getInstance().getReference("CartItems");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");

        //getCartStores();
        cartStoresRecycler.setHasFixedSize(true);
        cartStoresRecycler.setLayoutManager(new LinearLayoutManager(this));

        /*int orientation = this.getResources().getConfiguration().orientation;
        LinearLayoutManager layoutManager1;

        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            layoutManager1 = new GridLayoutManager(CartStoreActivity.this,2);
        else
            layoutManager1 = new GridLayoutManager(CartStoreActivity.this,4);

        cartStoresRecycler.setLayoutManager(layoutManager1);*/

        setAdsRV();
        iniAdBanners();
        getMyCartStores();

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

    private void getMyCartStores() {

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCartStoreIDs.clear();
                cartKeys.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    CartItems items = ds.getValue(CartItems.class);

                    assert items != null;
                    if (items.getUserID().equals(user.getUid())){
                        //key = ds.getKey();
                        cartKeys.add(ds.getKey());
                        myCartStoreIDs.add(items.getStoreID());

                    }
                }
                //cart keys and store ids have been added successfully

                cartStoreAdapter = new CartStoreAdapter(CartStoreActivity.this, cartKeys, myCartStoreIDs);
                cartStoresRecycler.setAdapter(cartStoreAdapter);
                cartStoreAdapter.notifyDataSetChanged();

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
                adsAdapter = new BannerAdsAdapter(CartStoreActivity.this, adsList);
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