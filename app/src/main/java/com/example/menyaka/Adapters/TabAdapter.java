package com.example.menyaka.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.menyaka.fragments.DynamicFragment;

import java.util.ArrayList;

public class TabAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;
    ArrayList<String> categoryList;
    Context context;

    public TabAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    public TabAdapter(@NonNull FragmentManager fm, int numOfTabs, ArrayList<String> categoryList) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.categoryList = categoryList;
    }

    public TabAdapter(@NonNull FragmentManager fm, int numOfTabs, ArrayList<String> categoryList, Context context) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.context = context;
        if(!categoryList.isEmpty()){
            this.categoryList = categoryList;
        }
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position >= 0) {
            return DynamicFragment.addFrag(position, categoryList);
        }else
            return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
