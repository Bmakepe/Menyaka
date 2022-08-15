package com.example.menyaka.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.PostAdapter;
import com.example.menyaka.Adapters.HotDealAdapter;
import com.example.menyaka.Adapters.ProductImageAdapter;
import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Adapters.ViewPagerAdapter;
import com.example.menyaka.HotDealActivity;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.MyStoresActivity;
import com.example.menyaka.ProductListActivity;
import com.example.menyaka.R;
import com.google.android.material.tabs.TabLayout;
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
import java.util.Objects;

public class HomeFragment extends Fragment {


    //for hot deals recycler
    private RecyclerView hotDealsRecycler;
    private HotDealAdapter hotDealAdapter;
    private TextView moreHotDeals;
    List<HotDeal> hotDealsList;

    //for stores
    private MyStoreAdapter retailAdapter;
    private RecyclerView retailRecycler;
    TextView moreStores;
    private ArrayList<MyStore> shopList;
    private ArrayList<String> myLikedStores;

    //database
    private FirebaseUser firebaseUser;
    private DatabaseReference hotDealsReference, retailRef, myStoresRef;

    public static final int ITEM_BEFORE_SPONSOR = 5;

    TabLayout homeLayout;
    ViewPager homePager;

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container,false);

        //for my favorite stores
        moreStores = view.findViewById(R.id.moreShops);
        retailRecycler = view.findViewById(R.id.retailRecycler);

        //for hot deals
        moreHotDeals = view.findViewById(R.id.seeMoreBTN);
        hotDealsRecycler = view.findViewById(R.id.hotDealsRecycler);

        //for showing the fragments
        homeLayout = view.findViewById(R.id.homeTabLayout);
        homePager = view.findViewById(R.id.homeViewPager);

        //database declarations
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myStoresRef = FirebaseDatabase.getInstance().getReference("MyStores");
        hotDealsReference = FirebaseDatabase.getInstance().getReference("HotDeals");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");

        //stores recycler view layout
        retailRecycler.hasFixedSize();
        retailRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        hotDealsRecycler.hasFixedSize();
        hotDealsRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        declareLists();
        loadFavoriteStores(retailRecycler);
        loadHotDeals(hotDealsRecycler);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new VideosFragment(), "Videos");
        viewPagerAdapter.addFragment(new PostsFragment(), "Posts");
        homePager.setAdapter(viewPagerAdapter);
        homeLayout.setupWithViewPager(homePager);

        moreHotDeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HotDealActivity.class);
                intent.putExtra("listStyle", "unfilteredList");
                startActivity(intent);
            }
        });
        
        moreStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), MyStoresActivity.class));
            }
        });

        return view;

    }

    private void declareLists() {

        //declaration for hot deals
        hotDealsList = new ArrayList<>();

        //declaration for store lists
        shopList = new ArrayList<>();
        myLikedStores = new ArrayList<>();
    }

    private void loadHotDeals(RecyclerView recyclerView) {

        hotDealsReference.limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotDealsList.clear();
                for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                    hotDealsReference.child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                                HotDeal hotDeal = dataSnapshot.getValue(HotDeal.class);
                                hotDealsList.add(hotDeal);
                            }
                            Collections.shuffle(hotDealsList);
                            hotDealAdapter = new HotDealAdapter(hotDealsList, getActivity());
                            recyclerView.setAdapter(hotDealAdapter);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFavoriteStores(RecyclerView recyclerView) {

        myStoresRef.child(firebaseUser.getUid()).limitToLast(10).addValueEventListener(new ValueEventListener() {
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

                        retailAdapter = new MyStoreAdapter(shopList, getActivity());
                        recyclerView.setAdapter(retailAdapter);

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

}
