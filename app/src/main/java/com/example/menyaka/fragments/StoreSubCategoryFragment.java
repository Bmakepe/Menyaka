package com.example.menyaka.fragments;

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

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.BannerAdsAdapter;
import com.example.menyaka.Adapters.ProductListAdapter;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Product;
import com.example.menyaka.R;
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

public class StoreSubCategoryFragment extends Fragment {

    String tabTitle, storeID;
    int tabPosition;

    ArrayList<String> categoryList;
    RecyclerView productsRecycler, adsRecycler;
    ArrayList<Product> products;
    ProductListAdapter adapter;

    DatabaseReference productRef, adsRef;

    EditText dynamicHeading;

    LinearLayoutManager layoutManager1;

    ArrayList<BannerAds> adsList;
    BannerAdsAdapter adsAdapter;

    public static StoreSubCategoryFragment addFrag(int position, ArrayList<String> categoryList, String storeID) {
        StoreSubCategoryFragment fragment = new StoreSubCategoryFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        args.putStringArrayList("tabTitles", categoryList);
        args.putString("storeID", storeID);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);

        productsRecycler = view.findViewById(R.id.dynamicRecycler);
        dynamicHeading = view.findViewById(R.id.dynamicHeader);
        adsRecycler = view.findViewById(R.id.dynamicAdsRecycler);

        tabPosition = getArguments().getInt("tabPosition", 0);

        categoryList = new ArrayList<>();
        products = new ArrayList<>();
        adsList = new ArrayList<>();

        categoryList = getArguments().getStringArrayList("tabTitles");
        storeID = getArguments().getString("storeID");

        for (int i = 0; i < categoryList.size(); i++){
            if (i == tabPosition){
                tabTitle = categoryList.get(i);
                dynamicHeading.setHint("Search " + tabTitle + " products");
            }
        }

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        adsRef = FirebaseDatabase.getInstance().getReference("AdBanners");
        productsRecycler.setHasFixedSize(true);
        productsRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        setAdsRV();
        getProductCategories();
        iniAdBanners();

        return view;
    }

    private void setAdsRV() {

        adsRecycler.hasFixedSize();
        layoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        adsRecycler.setLayoutManager(layoutManager1);
    }

    private void getProductCategories() {

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);

                    try{
                        if(product.getProductDepartment().equals(tabTitle)) {
                            if (product.getStoreId().equals(storeID))
                                products.add(product);
                        }
                    }catch (NullPointerException ignored){}
                }

                adapter = new ProductListAdapter(getContext(), products);
                productsRecycler.setAdapter(adapter);
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
