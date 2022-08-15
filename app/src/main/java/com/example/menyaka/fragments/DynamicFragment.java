package com.example.menyaka.fragments;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Adapters.TabAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Product;
import com.example.menyaka.ProductListActivity;
import com.example.menyaka.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kotlinx.coroutines.channels.ProduceKt;

public class DynamicFragment extends Fragment {

    String tabTitle;
    int tabPosition;

    ArrayList<String> categoryList;
    RecyclerView dynamicRecycler, adsRecycler;
    ArrayList<MyStore> stores;
    RetailAdapter adapter;

    DatabaseReference retailRef, adsRef;

    EditText dynamicHeading;

    LinearLayoutManager layoutManager1;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    public static DynamicFragment addFrag(int position, ArrayList<String> categoryList) {

        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        args.putStringArrayList("tabTitles", categoryList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);

        dynamicRecycler = view.findViewById(R.id.dynamicRecycler);
        dynamicHeading = view.findViewById(R.id.dynamicHeader);
        adsRecycler = view.findViewById(R.id.dynamicAdsRecycler);

        tabPosition = getArguments().getInt("tabPosition", 0);
        categoryList = new ArrayList<>();
        stores = new ArrayList<>();
        adsList = new ArrayList<>();

        categoryList = getArguments().getStringArrayList("tabTitles");

        for(int i = 0; i < categoryList.size(); i++){
            if(i == tabPosition){
                tabTitle = categoryList.get(i);
                dynamicHeading.setHint("Search " + tabTitle + " Store Listings");
            }
        }

        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");
        dynamicRecycler.setHasFixedSize(true);

        int orientation = this.getResources().getConfiguration().orientation;
        LinearLayoutManager layoutManager1;

        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            layoutManager1 = new GridLayoutManager(getActivity(),3);
        else
            layoutManager1 = new GridLayoutManager(getActivity(),6);

        dynamicRecycler.setLayoutManager(layoutManager1);

        setAdsRV();
        getRetailStores();
        iniAdBanners();

        return view;
    }

    private void setAdsRV() {

        adsRecycler.hasFixedSize();
        layoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        adsRecycler.setLayoutManager(layoutManager1);

    }

    private void getRetailStores() {

        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stores.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    MyStore retail = ds.getValue(MyStore.class);
                    try {
                        assert retail != null;
                        if(retail.getCategory().equals(tabTitle))
                            stores.add(retail);
                    }catch (NullPointerException ignored){}
                }

                adapter = new RetailAdapter(getActivity(), stores);
                dynamicRecycler.setAdapter(adapter);
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
                adsAdapter = new BannerAdsAdapter(getActivity(), adsList);
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