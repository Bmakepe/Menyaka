package com.example.menyaka.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.adapters.ViewPagerAdapter;
import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Adapters.TabAdapter;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StoresFragment extends Fragment {

    ArrayList<String> shopCategoryList;

    TabLayout shopCategoryTabs;
    ViewPager viewPager;
    TabAdapter adapter;

    public StoresFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stores, container,false);

        shopCategoryTabs = view.findViewById(R.id.shopCategoryTabs);
        viewPager = view.findViewById(R.id.shopsListPager);

        getCategories();

        return view;
    }

    private void getCategories() {
        shopCategoryList  = new ArrayList<>();
        final DatabaseReference catRef = FirebaseDatabase.getInstance().getReference("Retails");
        catRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shopCategoryList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    assert retail != null;
                    if(!shopCategoryList.contains(retail.getCategory())) {
                        shopCategoryList.add(retail.getCategory());
                    }
                }

                Collections.sort(shopCategoryList);

                for(int i= 0; i < shopCategoryList.size(); i++){
                    shopCategoryTabs.addTab(shopCategoryTabs.newTab().setText(shopCategoryList.get(i)));
                }

                try{
                    adapter = new TabAdapter(getChildFragmentManager(), shopCategoryTabs.getTabCount(), shopCategoryList, getContext());

                    viewPager.setAdapter(adapter);
                    viewPager.setOffscreenPageLimit(1);
                    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(shopCategoryTabs));

                    shopCategoryTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            viewPager.setCurrentItem(tab.getPosition());
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                }catch (IllegalArgumentException ignored){}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
