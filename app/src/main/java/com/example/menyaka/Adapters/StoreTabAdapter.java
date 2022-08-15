package com.example.menyaka.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.menyaka.fragments.HisPostsFragment;
import com.example.menyaka.fragments.HisVideosFragment;
import com.example.menyaka.fragments.ShopProfileFragment;

import java.util.ArrayList;

public class StoreTabAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;
    private String storeID;

    public StoreTabAdapter(@NonNull FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    public void addFragment(Fragment fragment, String title, String storeID){
        fragments.add(fragment);
        titles.add(title);
        this.storeID = storeID;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case 0:
                fragment = new ShopProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("storeID", storeID);
                fragment.setArguments(bundle);
                break;
            case 1:
                fragment = new HisPostsFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("hisUserID", storeID);
                fragment.setArguments(bundle1);
                break;
            case 2:
                fragment = new HisVideosFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("hisUserID", storeID);
                fragment.setArguments(bundle2);
                break;

            default:
                fragment = null;
                break;
        }

        return fragment;
    }

    public void addFragment(Fragment fragment, String title){
        fragments.add(fragment);
        titles.add(title);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
