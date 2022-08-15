package com.example.menyaka.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.menyaka.fragments.StoreSubCategoryFragment;

import java.util.ArrayList;

public class SubTabAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;
    ArrayList<String> categoryList;
    Context context;
    String storeID;

    public SubTabAdapter(@NonNull FragmentManager fm, int behavior, int numOfTabs) {
        super(fm, behavior);
        this.numOfTabs = numOfTabs;
    }

    public SubTabAdapter(@NonNull FragmentManager fm, int numOfTabs, ArrayList<String> categoryList, Context context, String storeID) {
        super(fm);
        this.numOfTabs = numOfTabs;
        if (!categoryList.isEmpty()){
            this.categoryList = categoryList;
        }
        this.context = context;
        this.storeID = storeID;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0){
            return StoreSubCategoryFragment.addFrag(position, categoryList, storeID);
        }else
            return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
